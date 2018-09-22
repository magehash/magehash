(ns server
  (:require [coast]
            [routes :refer [routes]])
  (:gen-class))

(def app (coast/app {:routes routes}))

(defn -main [& [port]]
  (coast/server app {:port port}))
