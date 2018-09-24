(ns home.index
  (:require [components :refer [icon input label submit error]]
            [coast :refer [form action-for redirect url-for rescue validate queue]]))

(defn site-form [request]
  (let [site-url-error? (some? (-> request :errors :site/url))]
    (form (action-for :site.new/action)
      (label :site/url "Your Site" {:classes (when site-url-error? '{red nil})})
      (input :text {:name :site/url
                    :classes (merge '{f -4
                                      pa 3}
                                    (when site-url-error?
                                      '{b [a --red]}))
                    :placeholder "https://fortune.500"
                    :required true})

      (submit {:class "bg-green white br1"} "Hash. Hash. Hash."))))

(defn site-assets [request]
  (let [url (-> request :session :site :site/url)]
    [:div {:class "mw9 center bg-white purple br3 pa4 shadow-4"}
     [:div {:id "site-assets"}
      [:div {:class "f2"}
       url]
      [:ul {:class "list pl0"}
       [:li
        [:span {:class "dib mr2 v-mid"}
         (icon "check")]
        [:span {:class ""}
         "whatever.js"]]]]]))

(defn view [request]
  [:div {:class "home"}
   [:div {:class "hero relative white flex flex-column justify-center"}
    [:div {:class "hero-overlay bg-purple-gradient"}]
    [:div {:class "z-2 mw8 center ph4"}
     [:div {:class "f-headline-l fw6 f1 fl w-100 white mb3"} "Watch your assets"]
     [:p {:class "f3-ns f4 tc-ns lh-copy white-80 center"}
      "Hash your js hourly and get notified of changes"
      (if (nil? (-> request :session :site))
       [:div {:class "mw6 center pt4"}
        (site-form request)]
       (site-assets request))]]]
   [:div {:class "fold h3 bg-white w-100 tc"}
    [:a {:class "db mt1" :href "#"}
     (icon "chevron-down")]]])

(defn action [request]
  (let [[site errors] (-> (:params request)
                          (select-keys [:site/url])
                          (validate [[:required [:site/url]]
                                     [:web-url [:site/url]]])
                          (rescue))]
    (if (nil? errors)
      (-> (redirect (url-for :home))
          (assoc :session {:site site}))
      (view (merge request errors)))))
