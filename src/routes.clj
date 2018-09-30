(ns routes)

(def routes [[:get "/" :home.index/view :home]
             [:get "/404" :error.not-found/view :404]
             [:get "/500" :error.server-error/view :500]
             [:post "/" :home.index/action :home/action]
             [:get "/site/:id/assets" :asset.list/view :asset/list]
             [:get "/sign-up" :auth.signup/view :auth]
             [:post "/sign-up" :auth.signup/action :auth/action]
             [:get "/sign-in" :auth.signin/view :auth]
             [:post "/sign-in" :auth.signin/action]])
