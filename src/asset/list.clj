(ns asset.list
  (:require [coast :refer [pull ok q]]
            [components :refer [icon]]))

(defn view [request]
  (let [site (pull [{:site/assets [:asset/name :asset/content :asset/hash]}]
                   [:site/id (-> request :params :id)])
        job (first (q '[:select jobs/finished-at
                        :where [jobs/id ?id]]
                      {:id (-> request :params :job-id)}))]
    (if (nil? (:jobs/finished-at job))
      (ok nil)
      (ok [:div {:class "bg-white purple br1 pa2 shadow-4 overflow-scroll w-100" :style "max-height: 300px"}
           (if (empty? (:site/assets site))
            [:div "There was a problem getting your js! ☹️"]
            [:ul {:class "list pl0"}
             (for [{:asset/keys [hash content name]} (:site/assets site)]
               [:li {:class "pb3"}
                [:div {:class "cf"}
                 [:div {:class "fl mr2 w1"}
                  (icon "check-circle" {:color "#06d19c"})]
                 [:div {:class "fl mr2 w3"}
                  (subs hash 0 7)]
                 [:div {:title (if (= name "inline") content name) :class "fl w5" :style "white-space: nowrap; text-overflow: ellipsis; overflow: hidden;"}
                  name]]])])]))))
