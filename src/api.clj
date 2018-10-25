(ns api
  (:require [coast :refer [rescue insert ok pull server-error delete]]
            [clojure.data.json :as json]))

(defn status [req]
  (ok {:status "ok"}))

(defn list-assets [req]
  (let [member-email (-> req :member/email)
        assets (pull '[{:member/properties
                        [{:property/site
                          [{:site/assets [asset/name asset/hash asset/updated-at asset/created-at]}]}]}]
                     [:member/email member-email])]
    (ok
     (->> (:member/properties assets)
          (map :property/site)
          (map :site/assets)))))

(defn add-site [req]
  (let [[site errors] (rescue
                       (insert {:site/url (-> req :params :url)}))
        [_ p-errors] (rescue
                      (insert {:property/site (:site/id site)
                               :property/member (:member/id req)}))]
    (if (every? nil? [errors p-errors])
      (ok (dissoc site :id))
      (server-error (merge errors p-errors)))))

(defn delete-site [req]
  (let [p (pull '[{:member/properties
                   [property/id
                    {:property/site [site/id site/url]}]}]
                [:member/id (:member/id req)])
        property (-> (filter #(= (-> % :property/site :site/url) (-> req :params :url)) (:member/properties p))
                     (first))
        [_ p-errors] (if (some? property)
                       (rescue (delete {:property/id (:property/id property)}))
                       [nil {:error {:message "Couldn't find the site you were looking"}}])
        [site errors] (if (some? property)
                        (rescue (delete (-> property :property/site (dissoc :site/url))))
                        [nil {:error {:message "Couldn't find the site you were looking"}}])]
    (if (nil? errors)
      (ok (dissoc site :id))
      (server-error errors))))

(extend-type java.sql.Timestamp
  json/JSONWriter
  (-write [date out]
    (json/-write (str date) out)))
