(ns home.dashboard
  (:require [coast :refer [action-for pull url-for]]
            [coast.time :as time]
            [components :refer [a]]))

(defn view [{member-id :member/id}]
  (let [{properties :member/properties} (pull '[{:member/properties
                                                 [property/id
                                                  {:property/site
                                                   [site/id site/url
                                                    {:site/assets [asset/id asset/name asset/hash asset/created-at]}]}]}]
                                              [:member/id member-id])]
    [:div {:class "cf ph2-ns dashboard-content"}
     [:div {:class "fl w-100 w-25-ns pa2 dash__sidebar"}
      [:ul {:class "list pl0 menu__list"}
       [:a {:href (url-for :dashboard)}
        [:li {:class "menu__list-element"}
         [:img {:src "/img/internet.svg", :height "20px"}]
         [:span {:class "ml1"}
          "Websites"]]]

       [:a {:href "mailto:me@ferrucc.io?Subject=Edit%20Magehash%20Account"}
        [:li {:class "menu__list-element"}
         [:img {:src "/img/settings.svg", :height "20px"}]
         [:span {:class "ml1"}
          "Settings"]]]

       [:a {:href "/sign-out"}
        [:li {:class "menu__list-element"}
         [:img {:src "/img/exit.svg", :height "20px", :fill "red"}]
         [:span {:class "ml1"}
          "Logout"]]]]]


     [:div {:class "fl w-100 w-75-ns pa2 dashboard__watch-websites-container"}
      [:div {:class "cf ph2-ns"}
       [:div {:class "fl w-100 w-75-ns pa2"}
        [:h1 {:class "f3 dash-title"}
         "Monitored Websites " (str "(" (count properties)  " / 100)")]
        [:h2 {:class "f4 lh-title pt0 mid-gray fw3 dash-subtitle"}
         "Your Websites"]]

       [:div {:class "fl w-100 w-25-ns pa2 mt4 button__add-website-div"}
        [:a {:class "f6 link dim br-pill ph3 pv2 mb2 dib white bg-dark-blue button__add-website"
             :href (url-for :property.create/view)}
         "Add New Website"]]]

      [:div {:class "dt dt--fixed hide-mobile"}
       [:div {:class "dt-row"}
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}
         "Url"]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}
         "# Assets"]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}
         "Last Change Detected"]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}]]

       (for [{site :property/site :as p} properties]
         [:div {:class "dt-row"}
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (:site/url site)]
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (count (:site/assets site))]
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (->> (:site/assets site)
                (map :asset/created-at)
                (map time/parse)
                (sort)
                (last))]
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (a {:action (action-for :property.delete/action p)
               :class "link blue underline"
               :data-confirm true}
             "Delete")]])]
      [:div {:class "mobile-websites__dashboard hide-desktop"}
       (for [{site :property/site :as p} properties]
        [:div {:class "card mobile-card__dashboard bg-white pa3 ma2 pb5"}
         [:div {:class "bg-white"}
          [:p
           [:span {:class "b"} "Url:  "]
           (:site/url site)]]
         [:div
          [:p
           [:span {:class "b"} "# Assets:  "]
           (count (:site/assets site))]]
         [:div
          [:p
           [:span {:class "b"} "Last Change Detected:  "]
           (->> (:site/assets site)
               (map :asset/created-at)
               (map time/parse)
               (sort)
               (last))]]
         [:div {:class "fr"}
          (a {:action (action-for :property.delete/action p)
              :class "cf link white bg-gray dim br-pill ph3 pv2"
              :data-confirm true}
            "Delete")]])]]]))
