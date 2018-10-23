(ns routes
  (:require [coast :refer [pull raise unauthorized wrap-routes]]
            [error.not-found]))

(defn wrap-admin [handler]
  (fn [{{:keys [member/email]} :session :as request}]
    (let [member (pull [:member/id :member/email]
                       [:member/email email])]
      ; TODO add boolean admin column on member table
      (if (contains? #{"swlkr@fastmail.com" "me@ferrucc.io" "ferruccio.balestreri@gmail.com"} (:member/email member))
        (handler (merge request member))
        (error.not-found/view request)))))

(defn wrap-auth [handler]
  (fn [{{:keys [member/email]} :session :as request}]
    (let [member (pull [:member/id :member/email]
                       [:member/email email])]
      (if (some? member)
        (handler (merge request member))
        (error.not-found/view request)))))

(def admin (wrap-routes wrap-admin
            [[:get "/admin/dashboard" :admin.dashboard/view]]))

(def private (wrap-routes wrap-auth
              [[:get "/dashboard" :home.dashboard/view :dashboard]
               [:get "/new-property" :property.create/view]
               [:post "/new-property" :property.create/action]
               [:post "/properties/:id/delete" :property.delete/action]
               [:post "/properties/:id/scrape" :property.scrape/action]
               [:get "/assets" :asset.index/view]]))

(def public [[:get "/"    :home.index/view :home]
             [:post "/"   :home.index/action :home/action]
             [:get "/404" :error.not-found/view :404]
             [:get "/500" :error.server-error/view :500]
             [:get "/sign-up" :auth.signup/view]
             [:post "/sign-up" :auth.signup/action]
             [:get "/sign-in" :auth.login/view]
             [:post "/sign-in" :auth.login/action]
             [:post "/sign-out" :auth.sign-out/action]])

(def routes (concat public private admin))
