;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.core
  "Main entry point for dredd"
  (:require [dredd.data :as data]
            [dredd.db-adapter.neo4j :as neo]
            [dredd.server :as server]
            [dredd.app :as app]))

;; Main entry point in the dredd. Please see README before studying
;; source code. 
;;
;;; Purpose of this ns is to:
;; - Establish connection to the database
;; - Initialize database if needed
;; - Start web server and run the dredd app
;;
;;; Usage:
;; - You use this ns to start dredd.
;; - Dredd can be stopped from web app admin interface
;; - If you stop dredd forcefully, database may get corrupted
;; - See README file for usage examples.
;;
;;; Code layout and notes:
;; - Application layout is handled in dredd.app
;; - Data initialization is handled in dredd.data
;; - Web server is handled in dredd.server
;; - Neo4j is used as a database

;;;; Public API

(defn start! []
  "Start dredd. Because shutdown-agents is called at the end,
  program should end after start finishes"
  (io!)
  (neo/with-db!
    (data/init!)
    (server/start-and-wait! app/handler))
  (shutdown-agents))

(defn -main [& args]
  "Entry point if you run the .jar"
  (start!))

;;;; Examples

(comment
  
  (start)
  (server/shutdown!)
  
)
