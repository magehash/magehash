(ns scraper
  (:require [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.set]
            [coast :refer [q pull transact]]
            [etaoin.api :refer [chrome quit wait go js-execute]])
  (:import [java.nio.charset Charset]
           [java.security MessageDigest]
           [java.net URI]
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

(defn external! [m]
  (let [client (http/make-client {:ssl-configurer sni-configure})
        {:keys [status body]} @(http/get (:src m) {:as :text :client client})]
    (if (= 200 status)
      (assoc m :sha1 (sha1 (or body ""))
               :content body)
      (assoc m :sha1 nil
               :content (str "Error retrieving content: (" status ") " body)))))

(defn tags! [url]
  (let [driver (chrome {:headless true :args ["--disable-dev-shm-usage"]})
        _ (go driver url)
        results (js-execute driver "var elements = document.getElementsByTagName(\"script\");
        var scripts = []

        for (var i = 0; i < elements.length; i++) {
          var el = elements[i];

          if (el.src) {
            scripts.push({src: el.src, name: el.src})
          } else {
            scripts.push({content: el.innerHTML, name: \"inline\"})
          }
        } return scripts;")]
       _ (quit driver)
    results))

(defn scripts! [url]
  (let [script-tags (tags! url)
        inline (->> (filter inline? script-tags)
                    (map inline))
        external (->> (filter #(not (inline? %)) script-tags)
                      (map external!))]
    (concat inline external)))

(defn save-assets [url]
  (let [site (pull '[site/url site/id
                     {:site/assets [asset/id asset/hash asset/name
                                    asset/content asset/site]}
                     {:site/properties [{:property/member [member/email]}]}]
                   [:site/url url])
        assets (->> (scripts! (:site/url site))
                    (map #(hash-map :asset/name (:name %)
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
        (str "hashed " (count assets) " on site " url)))))

(defn -main []
  (let [urls (->> (q '[:select site/url])
                  (map :site/url))]
    (doall
      (for [url urls]
        (save-assets url)))
    (transact {:cron/name "Asset Change Notifier"})))
