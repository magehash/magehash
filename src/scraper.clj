(ns scraper
  (:require [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.set]
            [coast :refer [q pull transact]]
            [clj-chrome-devtools.commands.page :as page]
            [clj-chrome-devtools.core :as chrome.core]
            [clj-chrome-devtools.commands.runtime :as runtime]
            [clj-chrome-devtools.events :as events]
            [clojure.data.json :as json]
            [clojure.stacktrace :as st])
  (:import [java.nio.charset Charset]
           [java.security MessageDigest]
           [java.net URI URL]
           [javax.net.ssl SSLEngine SSLParameters SNIHostName])
  (:gen-class))

(defn sha1 [s]
  (let [hashed
        (doto (java.security.MessageDigest/getInstance "SHA-1")
          (.reset)
          (.update (.getBytes s "UTF-8")))]
    (format "%040x" (new java.math.BigInteger 1 (.digest hashed)))))

(defn inline? [m]
  (contains? m :content))

(defn inline [m]
  (assoc m :sha1 (sha1 (:content m))))

; magic for sni ssl connections
(defn sni-configure
  [^SSLEngine ssl-engine ^URI uri]
  (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
    (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
    (.setSSLParameters ssl-engine ssl-params)))

(defn host [url]
  (let [url-obj (URL. url)]
    (str (.getProtocol url-obj) "://" (.getHost url-obj))))

(defn external! [s {:keys [src] :as m}]
  (let [url (if (string/starts-with? src "http") src (str (host s) src))
        client (http/make-client {:ssl-configurer sni-configure})
        {:keys [status body]} @(http/get url {:as :text :client client})]
    (if (= 200 status)
      (assoc m :sha1 (sha1 (or body ""))
               :content body)
      (assoc m :sha1 nil
               :content (str "Error retrieving content: (" status ") " body)))))

(defn tags! [c url]
  (json/read-str
   (:value (:result (runtime/evaluate c {:expression "var elements = document.scripts;
      var scripts = [];

      for (var i = 0; i < elements.length; i++) {
        var el = elements[i];

        if (el.src) {
          scripts.push({src: el.src})
        } else {
          scripts.push({content: el.innerHTML})
        }
      } JSON.stringify(scripts);"})))
   :key-fn keyword))

(defn scripts! [c url]
  (println "[scraper/scripts!] Attempting to get script tags from " url)
  (let [_ (events/with-event-wait c :page :dom-content-event-fired 30000
            (page/navigate c {:url url}))
        script-tags (tags! c url)
        _ (println "[scraper/scripts!] Scraped " (count script-tags) " from " url)
        inline (->> (filter inline? script-tags)
                    (map inline))
        external (->> (filter #(not (inline? %)) script-tags)
                      (map #(external! url %)))]
    (concat inline external)))

(defn save-assets [c url]
  (try
    (let [site (pull '[site/url site/id
                       {:site/assets [asset/id asset/hash asset/name
                                      asset/content asset/site]}
                       {:site/properties [{:property/member [member/email]}]}]
                     [:site/url url])
          assets (->> (scripts! c (:site/url site))
                      (map #(hash-map :asset/name (or (:src %) "inline")
                                      :asset/hash (:sha1 %)
                                      :asset/content (:content %)
                                      :asset/site (:site/id site))))
          old (set (map #(dissoc % :asset/id) (:site/assets site)))
          new (set assets)
          changed-assets (clojure.set/difference new old)]
      (if (empty? changed-assets)
        (println "[scraper/save-assets] No changes for site " url)
        (do
          (transact {:site/id (:site/id site)
                     :site/assets []})
          (transact {:site/id (:site/id site)
                     :site/assets new})
          ;(email {:to member-email :from "sean@magehash.com" :html (emails.change/html changed-assets) :text (emails.change/text changed-assets)})
          (println "[scraper/save-assets] Hashed " (count assets) " assets from site " url))))
    (catch Exception e
      (println "Url" url)
      (println "Message" (.getMessage e))
      (println "Data" (ex-data e))
      (println "Stacktrace" (with-out-str
                             (st/print-stack-trace e))))))

(defn -main [& [url]]
  (let [c (chrome.core/connect "localhost" 9222)
        _ (page/enable c {})
        urls (if (some? url)
               [url]
               (->> (q '[:select site/url])
                    (map :site/url)))]
    (doall
      (for [url urls]
        (save-assets c url)))
    (transact {:cron/name "Asset Change Notifier"})))
