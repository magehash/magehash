(ns server
  (:require [coast]
            [routes :refer [routes]]
            [components :refer [layout]])
  (:gen-class))

(def app (coast/app {:routes routes
                     :layout layout}))

(defn -main [& [port]]
  (coast/server app {:port port}))

(comment
  (-main))
