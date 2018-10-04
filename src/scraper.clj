(ns scraper
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [coast :refer [q pull delete insert]])
  (:import [java.nio.charset Charset]
           [java.security MessageDigest]
           [java.net URL]))

(defn dom [url]
  (html/html-snippet
    (:body @(http/get url))))

(defn tags [url selector]
  (-> (dom url)
      (html/select selector)))

(defn sha1 [s]
  (let [hashed
        (doto (java.security.MessageDigest/getInstance "SHA-1")
          (.reset)
          (.update (.getBytes s "UTF-8")))]
    (format "%040x" (new java.math.BigInteger 1 (.digest hashed)))))

(defn host [url]
  (let [url-obj (URL. url)]
    (str (.getProtocol url-obj) "://" (.getHost url-obj))))

(defn inline? [{{:keys [src]} :attrs}]
  (string/blank? src))

(defn content [s]
  (if (seq? s) (first s) s))

(defn inline [m]
  (let [new-content (content (:content m))]
    (merge m {:content new-content
              :name "inline"
              :sha1 (sha1 (or new-content ""))})))

(defn external [s {{:keys [src]} :attrs :as m}]
  (let [url (if (string/starts-with? src "http") src (str s src))
        body (:body @(http/get url {:as :text}))]
    (merge m {:name src
              :sha1 (sha1 (or body ""))})))

(defn scripts [url]
  (let [host-url (host url)
        script-tags (tags url [:script])
        inline (->> (filter inline? script-tags)
                    (map inline))
        external (->> (filter #(not (inline? %)) script-tags)
                      (map #(external host-url %)))]
    (concat inline external)))

(defn save-assets [url]
  (let [site (pull '[site/url site/id
                     {:site/assets [asset/id]}]
                   [:site/url url])
        assets (->> (scripts (:site/url site))
                    (map #(hash-map :asset/name (:name %)
                                    :asset/hash (:sha1 %)
                                    :asset/content (:content %)
                                    :asset/site (:site/id site))))]
    (when (some? (:site/assets site))
      (delete (:site/assets site)))
    (if (not (empty? assets))
      (insert assets)
      [])
    (str "hashed " (count assets) " on site " url)))

(defn -main []
  (let [urls (->> (q '[:select site/url])
                  (map :site/url))]
    (doall
      (for [url urls]
        (let [s (save-assets url)]
          @(http/post "https://hooks.slack.com/services/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"
                      {:form-params
                       {:payload
                        (format "{\"text\":\"[magehash] %s\"}" s)}}))))))
