(ns auth.sign-out
  (:require [coast :refer [redirect url-for]]))

(defn action [request]
  (-> (redirect (url-for :home))
      (assoc :session nil)))
