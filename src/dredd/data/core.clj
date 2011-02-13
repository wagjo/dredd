(ns dredd.data.core
  "Database stuff"
  (:require [dredd.db-adapter.neo4j :as neo]))

;; Public API

(defn init! []
  "Init neo database. Call this once when app starts"
  (io!)
  (neo/with-neo
    (neo/with-tx
      (doseq [rel-type [:users :itests :iquestions :tests :questions]]
        (when-not (neo/rel? (neo/root) rel-type)
          (neo/create-child! rel-type nil))))))
