(ns components
  (:require [coast :refer [css js]]))

(defn layout [request body]
  [:html
    [:head
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     (css "bundle.css")
     (js "bundle.js")]
    [:body
     body]])
