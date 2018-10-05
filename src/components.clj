(ns components
  (:require [coast :refer [action-for form css js url-for]]
            [clojure.string :as string]))

(defn icon
  ([n opts]
   (let [{:keys [color size]} opts]
     [:i {:class (str "fas fa-" (name n))
          :style (str "font-size: " (or size "16") "px; color: " (or color "#333"))}]))
  ([n]
   (icon n {})))

(defn a [{:keys [action data-confirm] :as params} & children]
  (if (nil? action)
    [:a params children]
    (form (if (true? data-confirm)
            (merge action {:class "dib mb0" :data-confirm true})
            (merge action {:class "dib mb0"}))
     [:button {:type "submit" :class "input-reset bg-transparent bn pointer"}
      [:div {:class (:class params)}
       children]])))

(defn nav [request]
  (when (not (contains? #{:auth.login/view :auth.login/action :auth.signup/view :auth.signup/action} (:coast.router/name request)))
    (if (some? (-> request :session :member/email))
     [:nav {:class "dt w-100 border-box pa3 ph5-ns fixed z-2 bg-blue-90"}
      [:a {:class "dtc v-mid white link dim w-third" :href (url-for :dashboard) :title "Dashboard"}
       [:img {:src "/img/logo-white.png"}]]

      [:div {:class "dtc v-mid w-75 tr"}
       (a {:class "link dim white f6 f5-ns dib mr3 mr4-ns" :action (action-for :auth.sign-out/action) :title "Sign Out"}
         "Sign Out")]]
     [:nav {:class "dt w-100 border-box pa3 ph5-ns fixed z-2 bg-blue-90"}
      [:a {:class "dtc v-mid white link dim w-third" :href (url-for :home) :title "Home"}
       [:img {:src "/img/logo-white.png"}]]

      [:div {:class "dtc v-mid w-75 tr"}
       [:a {:class "link dim white f6 f5-ns dib mr3 mr4-ns" :href (url-for :auth.login/view) :title "Login"}
         "Log In"]
       [:a {:class "link dim white f6 f5-ns dib" :href (url-for :auth.signup/view) :title "Sign Up"}
        "Sign Up"]]])))

(defn bundle-name [{route-name :coast.router/name}]
  (cond
    (contains? #{:auth.login/view
                 :auth.login/action
                 :auth.signup/view
                 :auth.signup/action} route-name) "auth"
    :else "bundle"))

(defn body-class [{route-name :coast.router/name}]
  (cond
    (contains? #{:auth.login/view :auth.login/action
                 :auth.signup/view :auth.signup/action} route-name) "colorset"
    :else ""))

(defn layout [request body]
  [:html
    [:head
     [:title "Magehash - Securing Your Static Assets"]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:meta {:name "msapplication-TileColor" :content "#da532c"}]
     [:meta {:name "theme-color" :content "#ffffff"}]
     [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "favicon_package/favicon-32x32.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "favicon_package/favicon-16x16.png"}]
     [:link {:rel "apple-touch-icon" :sizes "180x180" :href "favicon_package/apple-touch-icon.png"}]
     [:link {:rel "mask-icon" :href "favicon_package/safari-pinned-tab.svg" :color "#5bbad5"}]
     (css (str (bundle-name request) ".css"))
     (js (str (bundle-name request) ".js"))]
    [:body {:class (body-class request)}
     (nav request)
     body]])

(defn qualified-name [k]
  (when (ident? k)
    (let [k-ns (namespace k)
            k-name (name k)]
        (->> (filter #(not (string/blank? %)) [k-ns k-name])
             (string/join "/")))))

(defn class-str [[k v]]
  (cond
    (vector? v) (mapv #(vector (name k) (name %)) v)
    (number? v) [[(name k) (str v)]]
    :else [[(name k) (name (or v ""))]]))

(defn classes [m]
  (->> (map class-str m)
       (mapcat identity)
       (map #(string/join "" %))
       (string/join " ")))

(defn label
  ([k s m]
   (let [opts (dissoc m :classes)]
    [:label (merge opts {:for (qualified-name k)
                         :class (classes (merge {:db "" :mb 1 :mt 2 :w :-100} (:classes m)))})
      s]))
  ([k s]
   (label k s {}))
  ([k]
   (label k (-> k name string/capitalize))))

(defn input [k m]
  [:input (merge m {:type (name k)
                    :class (str "input-reset outline-0 w-100 pa2 ba b--black-20 " (:class m))
                    :name (qualified-name (:name m))})])

(def text (partial input :text))

(defn submit
  ([m s]
   [:input {:class (str "pointer grow input-reset outline-0 bn pv3 w-100 " (:class m))
            :type "submit"
            :name "submit"
            :value s}])
  ([s]
   (submit {} s)))

(defn error [s]
  [:small {:class "f6 db red"}
   s])

(defn table [& children]
  [:table {:class "f6 w-100"}
   children])

(defn th [& children]
  [:th {:class "fw6 tl pa3 bg-white"}
   children])

(defn tr [& children]
  [:tr {:class "stripe-dark"}
   children])

(defn td [& children]
  [:td {:class "pa3"}
   children])

(defn tbody [& children]
  [:tbody {:class "lh-copy"}
   children])

(defn card [& children]
  [:div {:class "bg-white br2 shadow-4 mb4"}
   children])

(defn content [& children]
  [:div {:class "pa3 overflow-auto"}
   children])

(defn title [& children]
  [:div {:class "f3-ns f5 lh-title bb b--black-10"}
   [:div {:class "pa3"}
    children]])
