(ns stripe
  (:require [org.httpkit.client :as http]
            [coast :refer [env]]
            [clojure.data.json :as json]))

(defn customer-request [m]
  (let [form-params {:source (:stripe/token m)
                     :description (str "Customer for " (:member/email m))}]
    {:url "https://api.stripe.com/v1/customers"
     :method :post
     :form-params form-params
     :basic-auth [(env :stripe-secret-key)]}))

(defn subscription-request [m]
  (let [form-params {:customer (:customer m)
                     "items[0][plan]" "plan_DnaZRny5kzBMnl"}]
    {:url "https://api.stripe.com/v1/subscriptions"
     :method :post
     :form-params form-params
     :basic-auth [(env :stripe-secret-key)]}))

(defn parse-response [{:keys [status body] :as m}]
  (let [parsed-body (json/read-str body :key-fn keyword)]
    (if (contains? #{200 201 202} status)
      parsed-body
      (throw (Exception. (str "[stripe/parse-response] " m))))))

(defn subscribe [m]
  (let [customer (-> (customer-request m)
                     (http/request)
                     (deref)
                     (parse-response))
        {:keys [status body]} @(http/request
                                 (subscription-request (merge m {:customer (:id customer)})))]
    (if (contains? #{200 201 202} status)
      body
      (throw
       (Exception.
        (str "[stripe/subscribe] Something went wrong authorizing payment: " body))))))
