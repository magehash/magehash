(ns scraper
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.edn :as edn])
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
