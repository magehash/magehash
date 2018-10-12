(ns auth.signup
  (:require [coast :refer [action-for form insert redirect rescue url-for validate]]
            [buddy.hashers :as hashers]
            [stripe]))

(defn view [{{:member/keys [email password confirm-password]} :errors}]
  [:div
   [:a {:href (url-for :home)}
    [:img {:class "auth__logo mw-100" :src "/img/logo-auth.png"}]]
   [:div {:class "mw9 center ph3-ns pa3 mw8-l"}
         [:div {:class "cf ph2-ns bg-white br4"}
          [:div {:class "auth-content fl w-50-l pa4 black"}
           [:div {:class "pitch"}
            [:h1 "Building the ultimate front-end security platform."]
            [:p "We take care of making sure that nothing bad happens when users connect to your website."]
            [:p "Our tools include:"]
            [:div {:class "pt1 pb1 pl3 pr3 br-4 pitch__features"}
             [:ul {:class "list pl3"}
              [:li {:class "mt3 mb3"}
               "🛡"
               [:strong {:class "ml2"}
                "Static Assets Integrity Protection"] "- So no one can hijack your payments and authentication pages."]
              [:li {:class "mt3 mb3"}
               "🔜🔐"
               [:strong {:class "ml2"}
                "TLS Certificate Verification"] "- So you can stay safe from MITM attacks!"]
              [:li {:class "mt3 mb3"}
               "🔜📈"
               [:strong {:class "ml2"}
                "Uptime Monitoring"] "- So you can always be available to your users"]
              [:li {:class "mt3 mb3"}
               [:strong "..and many more on the way"]]]]]]
          [:div {:class "fl w-50-l w-100 register__form-background"}
           (form (merge (action-for ::action)
                        {:class "auth__form mb0 register__form"
                         :id "sign-up-form"})
            [:div {:class "form__title"}
             [:p "Create an account"]]
            [:div {:class "form__item form__item--full form__item--email form__item--register"}
             [:label {:class "form__label" :for "email"}
              "Email Address"]
             [:input {:class "form__input" :type "email" :name "member/email" :id "email" :required "required"}]
             [:div {:class "form__error"}
              email]]

            [:div {:class "form__item form__item--register fl w-50-l pr1-l"}
             [:label {:class "form__label" :for "password"}
              "Password"]
             [:input {:class "form__input" :type "password" :name "member/password" :id "password" :required "required"}]
             [:div {:class "form__error"}
              password]]
            [:div {:class "form__item form__item--register fl w-50-l pl1-l"}
             [:label {:class "form__label" :for "password"}
              "Confirm Password"]
             [:input {:class "form__input" :type "password" :name "member/confirm-password" :id "confirm-password" :required "required"}]
             [:div {:class "form__error"}
              confirm-password]]
            [:div {:class "form__item form__item--full form__item--register w-100-l"}
             [:label {:class "form__label" :for "plan"}
              "Billing"]
             [:div {:class "black-80 b"}
               "$49/mo"]]
            [:div {:class "form__item form__item--full form__item--register"}
             [:label {:class "form__label" :for "email"}
              "Credit Card"]
             [:div {:id "card-element"}]
             [:div {:id "card-errors"}]]

            [:div {:class "form__item form__item--full form__item--actions fl w-100 mb3"}
             [:input {:class "form__button" :type "submit" :name "register" :value "Register"}]])]]

    [:p {:class "form-link-text"}
     "Already have an account? "
     [:a {:href (url-for :auth.login/view) :class "form-link"}
      "Login"]]]])

(defn action [{:keys [params] :as request}]
  (let [_ (stripe/subscribe params)
        [member errors] (-> (select-keys params [:member/email :member/password :member/confirm-password])
                            (validate [[:required [:member/email :member/password :member/confirm-password]]
                                       [:email [:email]]
                                       [:equal [:member/password :member/confirm-password]]
                                       [:min-length 10 :password]
                                       [:max-length 100 :password]])
                            (update :member/password hashers/derive)
                            (dissoc :member/confirm-password)
                            (insert)
                            (rescue))]
    (if (nil? errors)
      (-> (redirect (url-for :dashboard))
          (assoc :session (select-keys member [:member/email])))
      (view (merge request errors)))))
