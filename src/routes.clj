(ns routes)

(def routes [[:get "/" :home.index/view :home]
             [:get "/404" :error.not-found/view :404]
             [:get "/500" :error.server-error/view :500]
             [:post "/" :home.index/action :home/action]
             [:get "/site/:id/assets" :asset.list/view :asset/list]
             [:get "/sign-up" :auth.signup/view]
             [:post "/sign-up" :auth.signup/action]
             [:get "/sign-in" :auth.login/view]
             [:post "/sign-in" :auth.login/action]])
