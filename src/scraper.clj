(ns scraper
  (:require [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.set]
            [clojure.java.io :as io]
            [coast :refer [q pull transact uuid]]
            [clojure.data.json :as json]
            [clojure.stacktrace :as st]
            [chrome]
            [mailer])
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
  (not (string/blank? (:content m))))

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

(defn scripts! [tags url]
  (let [inline (->> (filter inline? tags)
                    (map inline))
        external (->> (filter #(not (inline? %)) tags)
                      (map #(external! url %)))]
    (concat inline external)))

(defn fmt-asset [a]
  (let [trimmed-content (if (= "inline" (:asset/name a))
                          (str (subs (:asset/content a) 0 140) "...")
                          "")]
    (str "- [" (:asset/hash a) "] " (:asset/name a) "\n" trimmed-content)))

(defn save-assets [url]
  (try
    (let [site (pull '[site/url site/id
                       {:site/assets [asset/id asset/hash asset/name
                                      asset/content asset/site]}
                       {:site/properties [{:property/member [member/email]}]}]
                     [:site/url url])
          _ (println "[scraper/save-assets] Attempting to get script tags from " url)
          tags (-> (chrome/with-connection [c "http://localhost:9222"]
                     (chrome/navigate c {:url url :timeout 30000})
                     (chrome/evaluate c "JSON.stringify([].slice.call(document.scripts).map(function(s) { return { src: s.src, content: s.innerHTML}}))"))
                   :result :result :value
                   (json/read-str :key-fn keyword)
                   (scripts! url))
          _ (println "[scraper/save-assets] Scraped" (count tags) "asset(s) from" url)
          assets (->> tags
                      (map #(hash-map :asset/name (or (:src %) "inline")
                                      :asset/hash (:sha1 %)
                                      :asset/content (:content %)
                                      :asset/site (:site/id site))))
          old (set (map #(dissoc % :asset/id) (:site/assets site)))
          new (set assets)
          changed-assets (clojure.set/difference new old)
          member-email (-> site :site/properties first :property/member :member/email)]
      (cond
        (and (empty? old) ; new site, don't send alert
             (not (empty? new)))
        (do
          (transact {:site/id (:site/id site)
                     :site/assets new})
          (println "[scraper/save-assets] Hashed" (count assets) "asset(s) from brand ✨ new ✨ site" url))

        (empty? changed-assets) ; nothing changed, don't send alert
        (println "[scraper/save-assets] No changes for site" url)

        :else
        (do
          (transact {:site/id (:site/id site)
                     :site/assets []})
          (transact {:site/id (:site/id site)
                     :site/assets new})
          (mailer/mail :txt/alert member-email {:site/url url
                                                :site/assets (->> (map fmt-asset changed-assets)
                                                                  (string/join "\n\n"))})
          (println "[scraper/save-assets] Hashed" (count assets) "asset(s) from site" url))))
    (catch Exception e
      (println "Url" url)
      (println "Message" (.getMessage e))
      (println "Data" (ex-data e))
      (println "Stacktrace" (with-out-str
                             (st/print-stack-trace e))))))

(defn scrape
  ([url]
   (let [urls (if (some? url)
                [url]
                (->> (q '[:select site/url])
                     (map :site/url)))]
     (doall
       (for [url urls]
         (save-assets url)))
     (transact {:cron/name "Asset Change Notifier"})))
  ([]
   (scrape nil)))

(defn -main [& [url]]
  (scrape url)
  (System/exit 0))
