(ns home.index
  (:require [components :refer [icon input label submit error]]
            [coast :refer [form action-for transact redirect url-for rescue validate queue]]
            [scraper]))

(defn site-form [request]
  (let [site-url-error? (some? (-> request :errors :site/url))]
    (form (action-for :site.new/action)
      (input :text {:name :site/url
                    :classes (merge '{f -4 pa 3}
                                    (when site-url-error?
                                      '{b [a --red]}))
                    :placeholder "https://magehash.com"
                    :required true
                    :autofocus ""
                    :value (-> request :session :site :site/url)})

      (submit {:class "bg-green white br1"} "Hash. Hash. Hash."))))

(defn site-assets [request]
  (let [{{:keys [job site]} :session} request]
    [:div
     [:div {:id "site" :data-url (url-for :asset/list {:job-id (str (:id job))
                                                       :id (:site/id site)})}]
     [:div {:id "assets"}
      "Hashing in progress..."]]))

(defn hero [request]
  [:div {:class "hero bg-purple-gradient relative white pb4"}
   [:div {:class "mw9 center ph4 pt5"}
    [:div {:class "pb4"}
     [:div {:class "f-headline-l fw6 tc-ns f1 white"} "Watch your assets"]
     [:div {:class "f3-ns f4 tc-ns lh-copy white-80 center"}
      "Hash your js hourly and get notified of changes"]]
    [:div {:class "pb4"}
     [:div {:class "mw6 center cf tc"}
      [:a {:class "no-underline dib mr3 w4 br-pill bg-green ba bw1 b--green tc grow ph3 pv2 white pointer"}
           ;:href (url-for :sign-up)}
        "Sign Up"]

      [:a {:class "no-underline dib w4 br-pill ba b--white bw1 bg-transparent tc grow ph3 pv2 white pointer"
           :href "#learn-more"}
        "Learn More"]]]

    [:div {:class "pb4"}
     [:div {:class "cf"}
      [:div {:class "fl w-third-ns w-100"}
       [:div {:class "bg-white pa2 mid-gray mh2-ns mb4 mb0-ns shadow-4 br1 cf"}
        [:p {:class "lh-copy measure mt0"}
         "British Airways announced it had suffered a breach resulting in the theft of customer data. In interviews with the BBC, the company noted that around 380,000 customers could have been affected and that the stolen information included personal and payment information"]
        [:cite {:class "f6 tracked fs-normal fr"}
         [:a {:class "black" :href "https://www.riskiq.com/blog/labs/magecart-british-airways-breach/"}
          "-RiskIQ"]]]]
      [:div {:class "fl w-third-ns w-100"}
       [:div {:class "bg-white pa2 mid-gray mh2-ns mb4 mb0-ns shadow-4 br1 cf"}
        [:p {:class "lh-copy measure mt0"}
         "A recent breach at Ticketmaster was just \"the tip of the iceberg\" of a wider, massive credit card skimming operation, new research has found.
At least 800 e-commerce sites are said to be affected, after they included code developed by third-party companies and later altered by hackers"]
        [:cite {:class "f6 tracked fs-normal fr"}
         [:a {:class "black" :href "https://www.riskiq.com/blog/labs/magecart-british-airways-breach/"}
          "-ZDNet"]]]]
      [:div {:class "fl w-third-ns w-100"}
       [:div {:class "bg-white pa2 mid-gray mh2-ns mb4 mb0-ns shadow-4 br1 cf"}
        [:p {:class "lh-copy measure mt0"}
         "The breach of Newegg shows the true extent of Magecart operators’ reach. These attacks are not confined to certain geolocations or specific industries—any organization that processes payments online is a target ... they integrated with the victim’s payment system"]
        [:cite {:class "f6 tracked fs-normal fr"}
         [:a {:class "black" :href "https://www.riskiq.com/blog/labs/magecart-newegg/"}
          "-RiskIQ"]]]]]]

    [:div {:class "cf"}
     [:div {:class "fl w-third-ns w-100"}
      [:div {:class "pa2 mh2-ns"}]]
     [:div {:class "fl w-third-ns w-100"}
      [:div {:class "pa2"}
       (site-form request)
       (when (some? (-> request :session :site))
        (site-assets request))]]]]])

(defn view [request]
  [:div
   (hero request)

   [:div {:class "bg-white pv6 ph3"}
    [:div {:class "cf mw8 center"}
     [:div {:class "fl w-50-ns w-100 h5 flex flex-column justify-center"}
      [:div {:class "ph2"}
       [:h1 {:class "f3 fw4 lh-title"}
        "Keep your sites' assets secure"]
       [:p {:class "f5 lh-copy mid-gray"}
        "Have confidence that your js assets are exactly as you left them."
        " No suprises if someone manages to commandeer your CMS or a CDN"]]]

     [:div {:class "fl w-50-ns w-100 h5 flex flex-column justify-center"}
      [:div {:class "ph4-ns"}
       [:img {:src "/img/safe-lander.svg"}]]]]]

   [:div {:class "bg-lightest-gray pv6 ph3"}
    [:div {:class "cf mw8 center"}

     [:div {:class "fl w-50-ns w-100 h5 flex flex-column justify-center"}
      [:div {:class "ph4-ns"}
       [:img {:src "/img/windows-lander.svg"}]]]

     [:div {:class "fl w-50-ns w-100 h5 flex flex-column justify-center"}
      [:div {:class "ph2"}
       [:h1 {:class "f3 fw4 lh-title"}
        "Watch up to 100 sites"]
       [:p {:class "f5 lh-copy mid-gray"}
        "Let magehash 🧙‍♂️ do the tedious work for you. "
        "Just enter your sites in the dashboard and they'll be watched every hour on the hour"]]]]]

   [:div {:class "bg-white pv6 ph3"}
    [:div {:class "cf mw8 center"}
     [:div {:class "fl w-50-ns w-100 h5 flex flex-column justify-center"}
      [:div {:class "ph2"}
       [:h1 {:class "f3 fw4 lh-title"}
        "Get notified of changes by email"]
       [:p {:class "f5 lh-copy mid-gray"}
        "Notifications happen when any js on your site changes. If you change your js, you can easily click a link"
        " and recreate a new hash for that asset"]]]

     [:div {:class "fl w-50-ns w-100 h5 flex flex-column justify-center"}
      [:div {:class "ph4-ns"}
       [:img {:src "/img/sync-lander.svg"}]]]]]])

(defn save-assets [url]
  (let [site (coast/pull '[site/url site/id
                           {:site/assets [asset/id]}]
                         [:site/url url])
        assets (->> (scraper/scripts (:site/url site))
                    (map #(hash-map :asset/name (:name %)
                                    :asset/hash (:sha1 %)
                                    :asset/content (:content %)
                                    :asset/site (:site/id site))))]
    (when (some? (:site/assets site))
      (coast/delete (:site/assets site)))
    (if (not (empty? assets))
      (coast/insert assets)
      [])))

(comment
  (coast/q '[:select jobs/id jobs/finished-at]))

(defn action [request]
  (let [[site errors] (-> (:params request)
                          (select-keys [:site/url])
                          (validate [[:required [:site/url]]
                                     [:web-url [:site/url]]])
                          (transact)
                          (rescue))]
    (if (nil? errors)
      (let [job (queue :home.index/save-assets (:site/url site))]
        (-> (redirect (url-for :home))
            (assoc :session {:site (select-keys site [:site/id :site/url])
                             :job (select-keys job [:id])})))
      (view (merge request errors)))))
