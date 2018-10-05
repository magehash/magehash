(ns admin.dashboard
  (:require [coast :refer [action-for q url-for]]
            [components :refer [card content title table th tr td tbody]]
            [coast.time :as time]))

(defn members-table [members]
  (card
    (title "Members")
    (content
      (table
        [:thead
         (tr
          (th "Email")
          (th "In Trial")
          (th "Joined")
          (th "Last Payment Date"))
         (tbody
           (for [{:member/keys [email created-at]} members]
             (tr
               (td email)
               (td)
               (td (time/fmt (time/local created-at) "MM/dd/YYYY"))
               (td))))]))))

(defn properties-table [properties]
  (card
    (title "Sites")
    (content
      (table
        [:thead
         (tr
          (th "Member")
          (th "Url")
          (th "# Assets")
          (th "Created")
          (th "Last Change Detected"))
         (tbody
           (for [{{url :site/url
                   assets :site/assets
                   created-at :site/created-at} :property/site
                  {email :member/email} :property/member} properties]
             (tr
               (td email)
               (td url)
               (td (count assets))
               (td (time/fmt (time/parse created-at) "MM/dd/YYYY"))
               (td (time/fmt
                    (->> (map #(or (:asset/updated-at %) (:asset/created-at %)) assets)
                         (map time/parse)
                         (sort)
                         (last))
                    "MM/dd/YYYY hh:mm:ss a")))))]))))

(defn jobs-table [jobs]
  (card
    (title "Jobs")
    (content
      (table
        [:thead
         (tr
          (th "Function")
          (th "Args")
          (th "Created")
          (th "Finished"))
         (tbody
           (for [{:jobs/keys [function args created-at finished-at]} jobs]
             (tr
               (td function)
               (td (str args))
               (td (time/fmt (time/local created-at) "MM/dd/YYYY hh:mm:ss a"))
               (td (time/fmt (time/local finished-at) "MM/dd/YYYY hh:mm:ss a")))))]))))

(defn plural [s val]
  (if (= 1 val)
    (clojure.string/join "" (drop-last s))
    s))

(defn ago [t]
  (let [since (time/since t)]
    (cond
      (> (:hours since) 0) (str (:hours since) " " (plural "hours" (:hours since)) " ago")
      (> (:minutes since) 0) (str (:minutes since) " " (plural "minutes" (:minutes since)) " ago")
      :else (str (:seconds since) " " (plural "seconds" (:seconds since)) " ago"))))

(defn cron-jobs-table [cron-jobs]
  (card
    (title "Cron")
    (content
      (table
        [:thead
         (tr
          (th "Name")
          (th "Last Run"))
         (tbody
           (for [{:cron/keys [name created-at updated-at]} cron-jobs]
             (tr
               (td name)
               (td (ago (or updated-at created-at))))))]))))

(defn view [request]
  (let [members (q '[:pull [member/email member/created-at
                            {:member/properties [{:property/site [site/url site/created-at
                                                                  {:site/assets [asset/id asset/created-at
                                                                                 asset/updated-at]}]}
                                                 {:property/member [member/email]}]}]])
        jobs (q '[:select jobs/id jobs/function jobs/args jobs/created-at jobs/finished-at
                  :order jobs/created-at desc])
        cron (q '[:select cron/name cron/created-at cron/updated-at
                  :order cron/created-at desc
                         cron/updated-at desc])]
    [:div {:class "pt6 mw8 center"}
     (members-table members)
     (properties-table (->> (map :member/properties members)
                            (mapcat identity)))
     (jobs-table jobs)
     (cron-jobs-table cron)]))
