(ns components
  (:require [coast :refer [css js url-for]]))

(defn icon
  ([name opts]
   (let [{:keys [size color class]} opts]
     [:div {:class class
            :data-icon (str "ei-" name)
            :data-size (or size "m")
            :style {:fill (or color "")}}]))
  ([name]
   (icon name {})))

(defn nav [request]
  [:nav {:class "dt w-100 border-box pa3 ph5-ns fixed z-2"}
   [:a {:class "dtc v-mid white link dim w-25" :href (url-for :home) :title "Home"}
    "Magehash 🧙‍♂️"]

   [:div {:class "dtc v-mid w-75 tr"}
    [:a {:class "link dim white-70 f6 f5-ns dib mr3 mr4-ns" :href "#" :title "About"}
     "About"]
    [:a {:class "link dim white-70 f6 f5-ns dib" :href "#" :title "Sign Up"}
     "Sign Up"]]])

(defn layout [request body]
  [:html
    [:head
     [:title "Magehash 🧙‍♂️"]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     (css "bundle.css")
     (js "bundle.js")]
    [:body
     (nav request)
     body]])
