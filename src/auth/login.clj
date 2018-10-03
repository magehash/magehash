(ns auth.login
  (:require [components :refer [icon input label submit error]]
            [coast :refer [form action-for transact redirect url-for rescue validate queue]]))

(defn view [request]
  [:main
   [:img {:class "auth__logo mw-100", :src "/img/logo-auth.png"}]
   [:div {:class "auth-content"}
    [:form {:class "auth__form"}
     [:div {:class "form__title"}
      [:p "Login"]]

     [:div {:class "form__item form__item--full form__item--email"}
      [:label {:class "form__label", :for "email"}
       "Email Address"]
      [:input {:class "form__input", :type "email", :name "email", :id "email", :required "required"}]
      [:div {:class "form__error"}]]

     [:div {:class "form__item form__item--full"}
      [:label {:class "form__label", :for "password"}
       "Password"]
      [:input {:class "form__input", :type "password", :name "password", :id "password", :required "required"}]
      [:div {:class "form__error"}]]

     [:div {:class "form__item form__item--full form__item--actions"}
      [:input {:class "form__button", :type "submit", :name "Login", :value "Login"}]
      [:a {:href "login.html", :class "form__forgot-link"}
       "Forgot your password?"]]]]


   [:div {:class "dashed__container"}
    [:p "Don't have an account yet? "
     [:a {:href "index.html", :class "form-link"}
      "Sign Up"]]]])
