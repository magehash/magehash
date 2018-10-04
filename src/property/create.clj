(ns property.create
  (:require [coast :refer [form transact delete action-for queue insert redirect rescue url-for validate]]
            [components :refer [error label text submit]]))

(defn view [{:keys [params errors]}]
  [:div {:class "pt6"}
   [:div {:class "mw6 center bg-white pa4"}
    (form (action-for ::action)
      (label :site/url "Site URL")
      (text {:name :site/url
             :value (:site/url params)})
      (error (:site/url errors))

      (submit {:class "bg-green white br1 mt2 pv2"} "Add Site"))]])

(defn action [{:keys [params]
               member-id :member/id :as request}]
  (let [[{site-id :site/id
          site-url :site/url} errors] (-> params
                                          (select-keys [:site/url])
                                          (validate [[:required [:site/url]]
                                                     [:web-url [:site/url]]])
                                          (transact)
                                          (rescue))]
    (if (nil? errors)
      (do
        (queue :scraper/save-assets site-url)
        (transact {:member/id member-id
                   :member/properties [{:property/site [:site/id site-id]}]})
        (redirect (url-for :dashboard)))
      (view (merge request errors)))))
