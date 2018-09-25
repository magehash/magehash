(ns home.index
  (:require [components :refer [icon input label submit error]]
            [coast :refer [form action-for transact redirect url-for rescue validate queue]]
            [scraper]))

(defn site-form [request]
  (let [site-url-error? (some? (-> request :errors :site/url))]
    (form (action-for :site.new/action)
      (input :text {:name :site/url
                    :classes (merge '{f -4
                                      pa 3}
                                    (when site-url-error?
                                      '{b [a --red]}))
                    :placeholder "https://magehash.com"
                    :required true
                    :value (-> request :session :site :site/url)})

      (submit {:class "bg-green white br1"} "Hash. Hash. Hash."))))

(defn site-assets [request]
  (let [url (-> request :session :site :site/url)]
    [:div
     [:div {:id "site" :data-id (-> request :session :site :site/id)}]
     [:div {:id "job" :data-id (-> request :session :job :id)}]
     [:div {:class "f3-ns f4 white mb2 mt5"}
      url]
     [:div {:id "assets"}
      "Hashing in progress..."]]))

(defn view [request]
  [:div {:class "home"}
   [:div {:class "hero bg-purple-gradient relative white h-100"}
    [:div {:class "mw9 center ph4 pt5 h-100"}
     [:div {:class "pb4"}
       [:div {:class "f-headline-l fw6 tc-ns f1 white"} "Watch your assets"]
       [:div {:class "f3-ns f4 tc-ns lh-copy white-80 center"}
        "Hash your js hourly and get notified of changes"]]
     [:div {:class "mw6 center"}
      (site-form request)
      (when (some? (-> request :session :site))
       (site-assets request))]]]])

(defn save-assets [url]
  (let [site (coast/pull '[site/url site/id
                           {:site/assets [asset/id]}]
                         [:site/url url])
        assets (->> (scraper/scripts (:site/url site))
                    (map #(hash-map :asset/name (:name %)
                                    :asset/hash (:sha1 %)
                                    :asset/content (:content %)
                                    :asset/site (:site/id site))))]
    (when (some? (:site/assets site))
      (coast/delete (:site/assets site)))
    (coast/insert assets)))

(defn action [request]
  (let [[site errors] (-> (:params request)
                          (select-keys [:site/url])
                          (validate [[:required [:site/url]]
                                     [:web-url [:site/url]]])
                          (transact)
                          (rescue))]
    (if (nil? errors)
      (let [job (queue :home.index/save-assets (:site/url site))]
        (-> (redirect (url-for :home))
            (assoc :session {:site (select-keys site [:site/id :site/url])
                             :job (select-keys job [:id])})))
      (view (merge request errors)))))
