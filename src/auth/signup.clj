(ns auth.signup
  (:require [coast :refer [action-for form url-for]]))

(defn view [request]
  [:main
   [:img {:class "auth__logo mw-100" :src "/img/logo-auth.png"}]
   [:div {:class "auth-content"}
    (form (merge (action-for ::action)
                 {:class "auth__form"})
     [:div {:class "form__title"}
      [:p "Create an account"]]

     [:div {:class "form__item form__item--full form__item--email"}
      [:label {:class "form__label" :for "email"}
       "Email Address"]
      [:input {:class "form__input" :type "email" :name "email" :id "email" :required "required"}]
      [:div {:class "form__error"}]]

     [:div {:class "form__item"}
      [:label {:class "form__label" :for "password"}
       "Password"]
      [:input {:class "form__input" :type "password" :name "password" :id "password" :required "required"}]
      [:div {:class "form__error"}]]

     [:div {:class "form__item"}
      [:label {:class "form__label" :for "password"}
       "Confirm Password"]
      [:input {:class "form__input" :type "password" :name "confirm-password" :id "confirm-password" :required "required"}]
      [:div {:class "form__error"}]]

     [:div {:class "form__item form__item--full form__item--actions"}
      [:input {:class "form__button" :type "submit" :name "register" :value "Register"}]])]


   [:p {:class "form-link-text"}
    "Already have an account? "
    [:a {:href (url-for :auth.login/view) :class "form-link"}
     "Login"]]])
