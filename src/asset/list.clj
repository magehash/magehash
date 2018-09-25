(ns asset.list
  (:require [coast :refer [pull ok q]]
            [components :refer [icon]]))

(defn view [request]
  (println (:params request))
  (let [site (pull [{:site/assets [:asset/name :asset/content :asset/hash]}]
                   [:site/id (-> request :params :id)])
        job (first (q '[:select jobs/finished-at
                        :where [jobs/id ?id]]
                      {:id (-> request :params :job-id)}))]
    (if (nil? (:jobs/finished-at job))
      (ok nil)
      (ok [:div {:class "bg-white purple br3 pa3 shadow-4"}
           (if (empty? (:site/assets site))
            [:div "Your site doesn't have js! 🎉"]
            [:ul {:class "list pl0"}
             (for [{:asset/keys [hash name]} (:site/assets site)]
               [:li {:class "pb3"}
                [:span name]
                [:div {:class "mt1"}
                 [:span {:class "green dib"}
                  (icon "check" {:size :s})]
                 [:span {:class "black v-top dib mt1"}
                  (subs hash 0 7)]]])])]))))

(first (q '[:select jobs/finished-at
            :where [jobs/id ?id]]
          {:id 30}))
