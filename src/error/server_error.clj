(ns error.server-error
  (:require [coast :refer [css js server-error]]))

(defn view [request]
  (server-error
    [:html
      [:head
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
       (css "bundle.css")
       (js "bundle.js")]
      [:body
       [:h1 "Something went wrong!"]]]))
