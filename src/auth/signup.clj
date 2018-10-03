(ns auth.signup
  (:require [coast :refer [action-for form insert redirect rescue url-for validate]]
            [buddy.hashers :as hashers]))

(defn view [{{:member/keys [email password confirm-password]} :errors}]
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
      [:input {:class "form__input" :type "email" :name "member/email" :id "email" :required "required"}]
      [:div {:class "form__error"}
       email]]

     [:div {:class "form__item"}
      [:label {:class "form__label" :for "password"}
       "Password"]
      [:input {:class "form__input" :type "password" :name "member/password" :id "password" :required "required"}]
      [:div {:class "form__error"}
       password]]

     [:div {:class "form__item"}
      [:label {:class "form__label" :for "password"}
       "Confirm Password"]
      [:input {:class "form__input" :type "password" :name "member/confirm-password" :id "confirm-password" :required "required"}]
      [:div {:class "form__error"}
       confirm-password]]

     [:div {:class "form__item form__item--full form__item--actions"}
      [:input {:class "form__button" :type "submit" :name "register" :value "Register"}]])]


   [:p {:class "form-link-text"}
    "Already have an account? "
    [:a {:href (url-for :auth.login/view) :class "form-link"}
     "Login"]]])

(defn action [{:keys [params] :as request}]
  (let [[member errors] (-> (select-keys params [:member/email :member/password :member/confirm-password])
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
