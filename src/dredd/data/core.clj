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

(defn collect-all-data []
  "Collect all data from database for export or backup"
  (let [user-ids (users/get-all-user-ids)
        collect-test (fn [user-id test-id]
                       (let [itest (itests/get-itest user-id test-id)]
                         ;; fill questions
                         (update-in itest [:questions] (fn [qs] (map (partial iquestions/get-iquestion user-id test-id) qs)))))
        collect-tests (fn [user-id] (map (partial collect-test user-id)
                                        (itests/get-all-users-itest-ids user-id)))]
    [user-ids
     (map collect-tests user-ids)]))
