(ns property.delete
  (:require [coast :refer [first! q redirect url-for rescue delete]]))

(defn action [{member-id :member/id
               {:keys [property-id]} :params}]
  (let [property (first!
                  (q '[:select property/id
                       :where [property/member ?member]
                              [property/id ?property]]
                     {:member member-id
                      :property property-id}))
        [_ errors] (rescue
                    (delete property))]
    (if (nil? errors)
      (redirect (url-for :dashboard))
      (-> (redirect (url-for :dashboard))
          (assoc :flash (str "Couldn't delete site "
                             (-> errors vals first)))))))
