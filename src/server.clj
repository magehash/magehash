(ns server
  (:require [coast]
            [routes :refer [routes api-routes]])
  (:gen-class))

(def app (coast/app {:routes/site routes
                     :routes/api api-routes}))

(defn -main [& [port]]
  (coast/server app {:port port}))

(comment
  (-main))
