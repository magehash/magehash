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
     [:div
      [:div {:class ""}
       [:h2 {:class "f4 lh-title pt0 mid-gray fw3 dash-subtitle"}
        "Your Sites"]]

      [:div {:class "dt dt--fixed hide-mobile"}
       [:div {:class "dt-row"}
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}
         "Url"]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}
         "# Assets"]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}
         "Last Change Detected"]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}]
        [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-light-gray"}]]

       (for [{site :property/site :as p} properties]
         [:div {:class "dt-row"}
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (:site/url site)]
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (count (:site/assets site))]
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           [:time (->> (:site/assets site)
                       (map :asset/created-at)
                       (map time/parse)
                       (map #(.toInstant %))
                       (sort)
                       (last))]]
          [:div {:class "dtc tc pv2 dashboard__table-content-desktop bg-white"}
           (a {:action (action-for :property.scrape/action p)
               :class "link blue underline"}
             "Hash")]
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
           [:time
            (->> (:site/assets site)
                (map :asset/created-at)
                (map time/parse)
                (map #(.toInstant %))
                (sort)
                (last))]]]
         [:div {:class "fr"}
          (a {:action (action-for :property.scrape/action p)
              :class "cf link white bg-gray dim br-pill ph3 pv2"}
            "Hash")]
         [:div {:class "fr"}
          (a {:action (action-for :property.delete/action p)
              :class "cf link white bg-gray dim br-pill ph3 pv2"
              :data-confirm true}
            "Delete")]])]]))
