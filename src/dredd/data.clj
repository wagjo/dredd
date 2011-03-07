;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data
  "General data operations"
  (:require [borneo.core :as neo]))

;; General operations on data stored in Neo4j database. See files in
;; data/ directory for more operations.

;;;; Public API

(defn init! []
  "Initializes neo database. Should be called once before web app starts."
  (io!)
  (neo/with-tx
    ;; Ensure main nodes are created. These nodes points to all other
    ;; data stored in database.
    (doseq [rel-type [:users :itests :iquestions :tests :questions]]
      (when-not (neo/rel? (neo/root) rel-type)
        (neo/create-child! rel-type nil)))))

;; Maintenance mode flag is stored as a root node property

(defn maintenance? []
  "Returns nil if dredd is not in maintenance mode."
  (:maintenance (neo/prop (neo/root))))

(defn set-maintenance!
  "Sets or removes maintenance mode. status can be true or false."
  ([] (set-maintenance! true))
  ([status]
     (if status
       (neo/set-properties! (neo/root) {:maintenance true})
       (neo/remove-properties! (neo/root) #{:maintenance}))))
