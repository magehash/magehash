(ns mailer
    (:require [postal.core :refer [send-message]]
              [clojure.string :as string]
              [clojure.java.io :as io]
              [coast :refer [env]])
    (:gen-class))

(def param-re #":([\w-_/?]+)")

(defn qualify-ident [k]
  (when (and (ident? k)
             (re-find #"-" (name k)))
    (let [[kns kn] (string/split (name k) #"-")]
      (keyword (or kns "") (or kn "")))))

(defn replacement [match m]
  (let [fallback (first match)
        k (-> match last keyword)
        s1 (get m k)
        s2 (get m (qualify-ident k))]
    (str (or s1 s2 fallback))))

(defn fmt [s m]
  (when (and (string? s)
             (or (nil? m) (map? m)))
    (string/replace s param-re #(replacement % m))))

; works like
; (send-email :txt/alert "some@email.com" {:site/url "https://site.com"})
; it should fill in the qualified keywords in the string
(defn mail [k to params]
  (if (or (not (qualified-ident? k))
          (nil? (io/resource (str "emails/" (name k) "." (namespace k)))))
    (throw (Exception. "The first arg needs to be a keyword of the file in resources/emails, like :txt/alert"))
    (let [body (-> (io/resource (str "emails/" (name k) "." (namespace k)))
                   (slurp)
                   (fmt params))
          creds {:host "smtp.mailtrap.io"
                 :user "12341d55f5c1ed"
                 :pass (env :email-password)}
          from "Team Magehash <team@magehash.com>"]
      (send-message creds {:from from
                           :to to
                           :subject "🚨 Magehash Alert! 🚨"
                           :body body}))))
