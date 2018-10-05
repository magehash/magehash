(ns scraper
  (:require [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.set]
            [coast :refer [q pull transact]]
            [clj-chrome-devtools.commands.dom :as dom]
            [clj-chrome-devtools.automation :as chrome]
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

(defn script! [node]
  (let [c (:connection @chrome/current-automation)
        {:keys [attributes]} (dom/get-attributes c node)
        {:strs [src]} (->> (partition 2 attributes)
                           (mapv vec)
                           (into {}))
        content (chrome/text-of node)
        m {:src src}]
    (if (and (some? content)
             (not (string/blank? content)))
      (assoc m :content content)
      m)))

(defn tags! [url]
  (chrome/to url)
  (->> (chrome/sel "script")
       (mapv #(script! %))))

(defn scripts! [url]
  (let [_ (chrome/start!)
        script-tags (tags! url)
        inline (->> (filter inline? script-tags)
                    (map inline))
        external (->> (filter #(not (inline? %)) script-tags)
                      (map #(external! url %)))]
    (concat inline external)))

(defn save-assets [url]
  (try
    (let [site (pull '[site/url site/id
                       {:site/assets [asset/id asset/hash asset/name
                                      asset/content asset/site]}
                       {:site/properties [{:property/member [member/email]}]}]
                     [:site/url url])
          assets (->> (scripts! (:site/url site))
                      (map #(hash-map :asset/name (or (:src %) "inline")
                                      :asset/hash (:sha1 %)
                                      :asset/content (:content %)
                                      :asset/site (:site/id site))))
          old (set (map #(dissoc % :asset/id) (:site/assets site)))
          new (set assets)
          changed-assets (clojure.set/difference new old)]
      (if (empty? changed-assets)
        (str "no changes for site " url)
        (do
          (transact {:site/id (:site/id site)
                     :site/assets []})
          (transact {:site/id (:site/id site)
                     :site/assets new})
          ;(email {:to member-email :from "sean@magehash.com" :html (emails.change/html changed-assets) :text (emails.change/text changed-assets)})
          (str "hashed " (count assets) " on site " url))))
    (catch Exception e
      (println "Url" url)
      (println "Message" (.getMessage e))
      (println "Data" (ex-data e))
      (println "Stacktrace" (with-out-str
                             (st/print-stack-trace e))))))

(defn -main []
  (let [urls (->> (q '[:select site/url])
                  (map :site/url))]
    (doall
      (for [url urls]
        (save-assets url)))
    (transact {:cron/name "Asset Change Notifier"})
    (System/exit 0))) ; force devtools connection to close
