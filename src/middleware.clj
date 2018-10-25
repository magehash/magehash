(ns middleware
  (:require [coast :refer [q pull unauthorized]]
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

(defn lower-case-keys [m]
  (->> (mapv (fn [[k v]] [(.toLowerCase k) v]) m)
       (into {})))

(defn wrap-api-auth [handler]
  (fn [request]
    (let [request (update request [:headers] lower-case-keys)
          api-token (get-in request [:headers "api-token"])
          member (first
                  (q '[:select member/id member/email member/admin
                       :where [member/api-token ?api-token]
                              [member/api-token != nil]]
                     {:api-token api-token}))]
      (if (some? member)
        (handler (merge request member))
        (unauthorized {:error {:message "I'm sorry Dave, I'm afraid I can't do that: https://www.youtube.com/watch?v=ARJ8cAGm6JE"}})))))
