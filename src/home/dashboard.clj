(ns home.dashboard)

(defn view [request]
  [:div {:class "cf ph2-ns dashboard-content"}
   [:div {:class "fl w-100 w-25-ns pa2 dash__sidebar"}
    [:ul {:class "list pl0 menu__list"}
     [:a {:href "/dash"}
      [:li {:class "menu__list-element"}
       [:img {:src "/img/internet.svg", :height "20px"}]
       [:span {:class "ml1"}
        "Websites"]]]

     [:a {:href "/ckcch"}
      [:li {:class "menu__list-element"}
       [:img {:src "/img/settings.svg", :height "20px"}]
       [:span {:class "ml1"}
        "Settings"]]]

     [:a {:href "/ciao"}
      [:li {:class "menu__list-element"}
       [:img {:src "/img/exit.svg", :height "20px", :fill "red"}]
       [:span {:class "ml1"}
        "Logout"]]]]]


   [:div {:class "fl w-100 w-75-ns pa2 dashboard__watch-websites-container"}
    [:div {:class "cf ph2-ns"}
     [:div {:class "fl w-100 w-75-ns pa2"}
      [:h1 {:class "f3 dash-title"}
       "Monitored Websites (2/100)"]
      [:h2 {:class "f4 lh-title pt0 mid-gray fw3 dash-subtitle"}
       "Your Websites"]]

     [:div {:class "fl w-100 w-25-ns pa2 mt4"}
      [:a {:class "f6 link dim br-pill ph3 pv2 mb2 dib white bg-dark-blue button__add-website"}
       "Add New Website"]]]

    [:div {:class "dt dt--fixed"}
     [:div {:class "dt-row"}
      [:div {:class "dtc tc pv2 bg-light-gray"}
       "Domain Name"]
      [:div {:class "dtc tc pv2 bg-light-gray"}
       "Monitoring Status"]
      [:div {:class "dtc tc pv2 bg-light-gray"}
       "Last Change Detected"]]

     [:div {:class "dt-row"}
      [:div {:class "dtc tc pv2 bg-white"}
       "google.com"]
      [:div {:class "dtc tc pv2 bg-lightest-blue"}
       "✅ Active"]
      [:div {:class "dtc tc pv2 bg-white"}
       "30 Sept. 2018 20:19:20"]]]]])
