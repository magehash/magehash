(ns asset.index
  (:require [coast :refer [first! q pull]]
            [components :refer [card content title table th tr td tbody]]
            [coast.time :as time]))

(defn view [{member-id :member/id :as req}]
  (let [props (q '[:pull [{:property/site [site/url
                                           {:site/assets [asset/hash asset/name asset/updated-at asset/created-at]}]}]
                   :where [property/member ?member/id]]
                 {:member/id member-id})]
    [:div
     [:h2 {:class "f4 lh-title pt0 mid-gray fw3 dash-subtitle"}
      "Assets"]
     (for [prop props]
       (card
         (title (-> prop :property/site :site/url))
         (content
           (table
             [:thead
              (tr
               (th "Asset")
               (th "SHA1")
               (th "Last Change Detected"))
              (tbody
                (for [{:asset/keys [hash name updated-at created-at]} (-> prop :property/site :site/assets)]
                  (tr
                    (td name)
                    (td hash)
                    (td [:time
                         (-> (or updated-at created-at)
                             time/parse
                             .toInstant)]))))]))))]))
