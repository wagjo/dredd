;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data
  "General operations on data."
  (:require [borneo.core :as neo]
            [dredd.local-settings :as settings]))

;; General operations on data stored in Neo4j database. See files in
;; data/ directory for more operations.

;;;; Implementation details

(defn- create-if-not-exists!
  "Create child only if there is no such relationship from parent node."
  ([rel-type]
     (create-if-not-exists! (neo/root) rel-type nil))
  ([rel-type props]
     (create-if-not-exists! (neo/root) rel-type props))
  ([parent rel-type props]
     (when-not (neo/rel? parent rel-type)
       (neo/create-child! rel-type props))))

;;;; Public API

(defn init!
  "Initializes neo database. Should be called once before web app starts."
  []
  (io!)
  (neo/with-tx
    ;; Ensure main nodes are created. These nodes points to all other
    ;; data stored in database.
    (doseq [rel-type [:users :itests :iquestions :tests :questions]]
      (create-if-not-exists! rel-type))
    ;; create admin node
    (create-if-not-exists! :admin (select-keys settings/admin [:uid :cn :role]))
    ;; create guest node
    (create-if-not-exists! :guest (select-keys settings/guest [:uid :cn :role]))))

;; Maintenance mode flag is stored as a root node property

(defn maintenance? []
  "Returns nil if dredd is not in maintenance mode."
  (neo/prop (neo/root) :maintenance))

(defn set-maintenance!
  "Sets or removes maintenance mode. status can be true or false."
  ([] (set-maintenance! nil))
  ([status]
     (neo/set-prop! (neo/root) :maintenance status)))
