(ns components
  (:require [coast :refer [css js url-for]]
            [clojure.string :as string]))

(defn icon
  ([n opts]
   (let [{:keys [color size]} opts]
     [:i {:class (str "fas fa-" (name n))
          :style (str "font-size: " (or size "16") "px; color: " (or color "#333"))}]))
  ([n]
   (icon n {})))

(defn nav [request]
  [:nav {:class "dt w-100 border-box pa3 ph5-ns fixed z-2 bg-blue-90"}
   [:a {:class "dtc v-mid white link dim w-third" :href (url-for :home) :title "Home"}
    [:img {:src "img/logo-white.png"}]]

   [:div {:class "dtc v-mid w-75 tr"}
    [:a {:class "link dim white-70 f6 f5-ns dib mr3 mr4-ns" :href "#" :title "About"}
     "About"]
    [:a {:class "link dim white-70 f6 f5-ns dib mr3 mr4-ns" :href "/sign-in" :title "Login"}
      "Log In"]
    [:a {:class "link dim white-70 f6 f5-ns dib" :href "/sign-up" :title "Sign Up"}
     "Sign Up"]]])

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
     (css "bundle.css")
     (js "bundle.js")]
    [:body
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
  (let [opts (dissoc m :classes)]
    [:input (merge opts {:type (name k)
                         :class (classes
                                 (merge '{input-reset nil
                                          outline -0
                                          w -100
                                          pa 2
                                          b [a --transparent]}
                                        (:classes m)))
                         :name (qualified-name (:name m))})]))

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
