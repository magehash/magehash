(ns routes
  (:require [coast :refer [pull raise unauthorized wrap-routes]]))

(defn wrap-auth [handler]
  (fn [{{:keys [member/email]} :session :as request}]
    (let [member (pull [:member/id :member/email]
                       [:member/email email])]
      (if (some? member)
        (handler (merge request member))
        (raise {:404 true})))))

(def private (wrap-routes wrap-auth
              [[:get "/dashboard" :home.dashboard/view :dashboard]]))

(def public [[:get "/"    :home.index/view :home]
             [:post "/"   :home.index/action :home/action]
             [:get "/404" :error.not-found/view :404]
             [:get "/500" :error.server-error/view :500]
             [:get "/site/:id/assets" :asset.list/view :asset/list]
             [:get "/sign-up" :auth.signup/view]
             [:post "/sign-up" :auth.signup/action]
             [:get "/sign-in" :auth.login/view]
             [:post "/sign-in" :auth.login/action]
             [:post "/sign-out" :auth.sign-out/action]])

(def routes (concat public private))
