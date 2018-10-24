(ns middleware
  (:require [coast :refer [pull]]
            [error.not-found]))

(defn wrap-admin [handler]
  (fn [{{:keys [member/email]} :session :as request}]
    (let [member (pull '[member/id member/email member/admin]
                       [:member/email email])]
      (if (true? (:member/admin member))
        (handler (merge request member))
        (error.not-found/view request)))))

(defn wrap-auth [handler]
  (fn [{{:keys [member/email]} :session :as request}]
    (let [member (pull '[member/id member/email member/admin]
                       [:member/email email])]
      (if (some? member)
        (handler (merge request member))
        (error.not-found/view request)))))
