(ns dredd.data.iquestions
  "Database stuff"
  (:require [dredd.db-adapter.neo4j :as neo]
            [dredd.data.itests :as itests]))

;; iQuestions

(defn get-iquestion-node [user-id test-id question-id]
  "Get iquestion"
  (when-let [itest (itests/get-itest-node user-id test-id)]
    (first (neo/find-by-props
            itest
            :iquestion
            {:id question-id}))))

(defn get-iquestion [user-id test-id question-id]
  "Get iquestion"
    (when-let [result (get-iquestion-node user-id test-id question-id)]
      (neo/prop result)))

(defn rank-iquestion! [user-id test-id question-id result comment]
  (let [q (get-iquestion-node user-id test-id question-id)]
    (when q
      (neo/with-tx
        (neo/set-properties! q {:result result :comment comment})))))
