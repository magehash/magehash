(ns server-test
  (:require [clojure.test :refer [deftest testing is]]
            [server :refer [app]]))

(deftest home-test
  (testing "home route"
    (let [response (app {:uri "/" :request-method :get})]
      (is (= (:status response) 200)))))

(deftest not-found-test
  (testing "not-found route"
    (let [response (app {:uri "/not-found" :request-method :get})]
      (is (= (:status response) 404)))))
