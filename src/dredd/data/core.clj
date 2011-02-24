(ns dredd.data.core
  "Database stuff"
  (:require [dredd.db-adapter.neo4j :as neo]
            [dredd.data.users :as users]
            [dredd.data.itests :as itests]
            [dredd.data.iquestions :as iquestions]))

;; Public API

(defn init! []
  "Init neo database. Call this once when app starts"
  (io!)
    (neo/with-tx
      (doseq [rel-type [:users :itests :iquestions :tests :questions]]
        (when-not (neo/rel? (neo/root) rel-type)
          (neo/create-child! rel-type nil)))))

(defn maintenance? []
  (:maintenance (neo/prop (neo/root))))

(defn set-maintenance!
  ([] (set-maintenance! true))
  ([status] (if status
              (neo/set-properties! (neo/root) {:maintenance true})
              (neo/remove-properties! (neo/root) #{:maintenance}))))
