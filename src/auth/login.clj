(ns auth.login
  (:require [coast :refer [action-for form redirect url-for pull]]
            [buddy.hashers :as hashers]))

(defn view [{{:member/keys [email]} :params
             {email-error :member/email
              password-error :member/password} :errors}]
  [:main
   [:a {:href (url-for :home)}
    [:img {:class "auth__logo mw-100" :src "/img/logo-auth.png"}]]
   [:div {:class "auth-content"}
    (form (merge (action-for ::action)
                 {:class "auth__form"})
     [:div {:class "form__title"}
      [:p "Login"]]

     [:div {:class "form__item form__item--full form__item--email"}
      [:label {:class "form__label" :for "email"}
       "Email Address"]
      [:input {:value email :class "form__input" :type "email" :name "member/email" :id "email" :required "required"}]
      [:div {:class "form__error"}
       email-error]]

     [:div {:class "form__item form__item--full"}
      [:label {:class "form__label" :for "password"}
       "Password"]
      [:input {:class "form__input" :type "password" :name "member/password" :id "password" :required "required"}]
      [:div {:class "form__error"}
       password-error]]

     [:div {:class "form__item form__item--full form__item--actions"}
      [:input {:class "form__button" :type "submit" :name "Login" :value "Login"}]
      [:a {:href "" :class "form__forgot-link"}
       "Forgot your password?"]])]


   [:div {:class "dashed__container"}
    [:p "Don't have an account yet? "
     [:a {:href (url-for :auth.signup/view) :class "form-link"}
      "Sign Up"]]]])

(defn action [{{params-email :member/email
                params-password :member/password} :params :as request}]
  (let [{member-password :member/password
         member-email :member/email} (pull [:member/email :member/password]
                                           [:member/email params-email])]
    (if (hashers/check params-password member-password)
      (-> (redirect (url-for :dashboard))
          (assoc :session {:member/email member-email}))
      (view (merge request {:errors {:member/email "Email or password is incorrect"
                                     :member/password "Email or password is incorrect"}})))))
