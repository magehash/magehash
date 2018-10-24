(ns asset.index
  (:require [coast :refer [first! q pull]]
            [clojure.string :as string]
            [coast.time :as time]
            [components :refer [card content title table th tr td tbody]]))

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
                    (td (if (string/blank? name) "inline" name))
                    (td hash)
                    (td [:time
                         (-> (or updated-at created-at)
                             time/parse
                             .toInstant)]))))]))))]))
