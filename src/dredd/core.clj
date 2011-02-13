(ns dredd.core
  "Main entry point to the app"
  (:require [dredd.app :as app]
            [dredd.data.core :as data]
            [ring.adapter.jetty :as adapter]))

;; Server management

(defonce *server* nil)

(defn start []
  "start the app"
  (data/init!)
  (let [server (adapter/run-jetty app/app {:port 3000 :join? false})]
    (alter-var-root #'*server* (fn [_] server))))

(defn stop []
  "stop the running app"
  (.stop *server*))

(defn start-and-wait []
  "start the app and block"
  (data/init!)
  (adapter/run-jetty app/app {:port 3000}))

;; Examples

(comment

  (start)

  (stop)
  
)
