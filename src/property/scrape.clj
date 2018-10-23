(ns property.scrape
  (:require [coast :refer [first! q redirect url-for rescue queue]]))

(defn action [{member-id :member/id
               {:keys [id]} :params}]
  (let [property (first!
                  (q '[:pull [property/id {:property/site [site/url]}]
                       :where [property/member ?member]
                              [property/id ?property]]
                     {:member member-id
                      :property id}))]
    (queue :scraper/scrape (-> property :property/site :site/url))
    (redirect (url-for :dashboard))))
