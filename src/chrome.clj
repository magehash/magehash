(ns chrome
  (:require [clojure.core.async :refer [>! <!! <! go chan timeout alts!!]]
            [clojure.string :as string]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [gniazdo.core :as ws]))

(defn !capitalize [s]
  (if (< (count s) 2)
    (.toLowerCase s)
    (str (.toLowerCase (subs s 0 1))
         (.toLowerCase (subs s 1)))))

(defn kebab->camel [s]
  (when (or (ident? s)
            (string? s))
    (let [parts (string/split (name s) #"-")
          caps (map string/capitalize (rest parts))]
      (string/join "" (concat [(!capitalize (first parts))] caps)))))

(defn camel->kebab [s]
  (when (string? s)
    (let [s1 (-> (string/replace (name s) #"([A-Z]+)([A-Z][a-z])" "$1-$2")
                 (string/replace #"([a-z]\d)([A-Z])" "$1-$2"))]
      (string/lower-case s1))))

(def id (atom 0))
(def commands-ch (chan))
(def events-ch (chan))

(defn on-receive [s]
  (go
    (let [m (json/read-str s :key-fn #(-> % camel->kebab keyword))]
      (if (contains? m :id)
        (>! commands-ch m)
        (>! events-ch m)))))

(defn connect [s]
  (let [url (-> (:body @(http/get (str s "/json/list")))
                (json/read-str)
                (first)
                (get "webSocketDebuggerUrl"))
        client (ws/client)]
    (doto (.getPolicy client)
      (.setIdleTimeout 0)
      (.setMaxTextMessageSize (* 100 1024 1024)))
    (.start client)
    (ws/connect url :on-receive on-receive
                    :client client)))

(defn domain [k]
  (string/capitalize
    (namespace k)))

(defn method [k]
  (str (domain k) "." (kebab->camel (name k))))

(defn command
  ([conn k params]
   (let [msg (json/write-str {:method (method k)
                              :id (swap! id inc)
                              :params params})]
     (ws/send-msg conn msg)
     (<!! commands-ch)))
  ([conn k]
   (command conn k {})))

(defn event
  ([_ k m _]
   (let [timeout-ch (timeout (or (:timeout m) 5000))
         read! #(alts!! [timeout-ch events-ch])]
     (loop [[val chan] (read!)]
       (cond
         (= chan timeout-ch) (throw (ex-info "Timeout! Specified event was not received."
                                             {:method method :event k :timeout (:timeout m)}))

         (and (= chan events-ch)
              (= (method k) (:method val))) val

         :default (recur (read!))))))
  ([a k b]
   (event a k {} b)))

(defn navigate [c m]
  (command c :page/enable)
  (event c :page/frame-stopped-loading (select-keys m [:timeout])
    (command c :page/navigate (select-keys m [:url]))))

(defn evaluate [c s]
  (command c :runtime/enable)
  (command c :runtime/evaluate {:expression s}))

(defmacro with-connection [[b-name b-val] & body]
  `(let [conn# (connect ~b-val)
         ~b-name conn#
         result# (do ~@body)]
     (ws/close conn#)
     result#))
