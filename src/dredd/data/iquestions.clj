(ns dredd.data.iquestions
  "Database stuff"
  (:require [dredd.db-adapter.neo4j :as neo]
            [dredd.data.itests :as itests]))

;; iQuestions

(defn get-iquestion-node [user-id test-id question-id]
  "Get iquestion"
  (neo/with-neo
    (first (neo/find-by-props
            (itests/get-itest-node user-id test-id)
            :iquestion
            {:id question-id}))))

(defn get-iquestion [user-id test-id question-id]
  "Get iquestion"
  (neo/with-neo
    (when-let [result (get-iquestion-node user-id test-id question-id)]
      (neo/prop result))))
