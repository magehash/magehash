(ns components
  (:require [coast :refer [css js url-for]]
            [clojure.string :as string]))

(defn icon
  ([name opts]
   (let [{:keys [size color class]} opts]
     [:div {:class class
            :data-icon (str "ei-" name)
            :data-size (or size "m")
            :style {:fill (or color "")}}]))
  ([name]
   (icon name {})))

(defn nav [request]
  [:nav {:class "dt w-100 border-box pa3 ph5-ns fixed z-2"}
   [:a {:class "dtc v-mid white link dim w-third" :href (url-for :home) :title "Home"}
    "Magehash 🧙‍♂️"]

   [:div {:class "dtc v-mid w-75 tr"}
    [:a {:class "link dim white-70 f6 f5-ns dib mr3 mr4-ns" :href "#" :title "About"}
     "About"]
    [:a {:class "link dim white-70 f6 f5-ns dib" :href "#" :title "Sign Up"}
     "Sign Up"]]])

(defn layout [request body]
  [:html
    [:head
     [:title "Magehash 🧙‍♂️"]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
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
                              ;:class (merge (str "input-reset outline-0 w-100 pa2 ba b--transparent " (:class m)))
                         :class (classes
                                 (merge '{:input-reset nil
                                          :outline :-0
                                          :w :-100
                                          :pa :2
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
