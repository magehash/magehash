(ns home.index
  (:require [components :refer [icon]]))

(defn view [request]
  [:div {:class "home"}
   [:div {:class "hero relative tc white flex flex-column justify-center"}
    [:div {:class "hero-overlay bg-purple-gradient"}]
    [:div {:class "z-2 mw8 center"}
     [:div {:class "f-headline-l f2 fw7"} "Track your js"]
     [:div {:class "f3-ns f4 lh-copy mw7 fw2 white-80 center"}
      "Magecart? More like mage smart."
      [:br]
      "Hash your js hourly and get notified when it changes"]]]
   [:div {:class "fold h3 bg-white w-100 tc"}
    [:a {:class "db mt1" :href "#"}
     (icon "chevron-down" {:color "black"})]]])
