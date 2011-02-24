;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.core
  "Main entry point to the dredd"
  (:require [dredd.app :as app]
            [dredd.data.core :as data]
            [dredd.db-adapter.neo4j :as neo]
            [ring.adapter.jetty :as adapter]))

;;;; Overview

;; Main entry point in the dredd. Its purpose is to:
;; - Establish connection to database
;; - Initialize database if needed
;; - Start and manage web server which runs the dredd
;;
;;; Usage notes
;; - You use this ns to start and stop dredd.
;; - See README file for usage examples.
;; - Servlets and Application containers are not supported.
;; - Start dredd on a dedicated port and use reverse proxy to
;;   make it public. That way you can also enable TLS for dredd.
;;
;;; Code layout and notes
;; - Application layout is handled in dredd.app
;; - Data initialization is handled in dredd.data.core
;; - Jetty is used as a web server
;; - Neo4j is used as a database


;;;; Implementation details

(defonce *server* nil)

;;;; Public API

;;; Server management

(defn start []
  "Start the app"
  (neo/start-neo)
  (data/init!)
  (let [server (adapter/run-jetty app/app {:port 3000 :join? false})]
    (alter-var-root #'*server* (fn [_] server))))

(defn stop []
  "Stop the running app"
  (.stop *server*)
  (neo/stop-neo))

(defn start-and-wait []
  "Start the app and block"
  (start)
  (.join *server*)
  (stop))

;;;; Examples

(comment

  (start)

  (stop)

  (start-and-wait)
  
)
