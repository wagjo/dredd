(ns dredd.data.itests
  "Database stuff"
  (:require [dredd.db-adapter.neo4j :as neo]
            [dredd.data.users :as users]
            [dredd.data.questions :as questions]
            [clj-time.core :as clj-time]))

;; Helper Vars

(defn- node-itests []
  "Get itests node"
  (neo/go (neo/root) :itests))

(defn- node-iquestions []
  "Get iquestions node"
  (neo/go (neo/root) :iquestions))

;; iTests

(defn itest? [user-id test-id]
  "Determine whether user has instance of some test"
    (not (empty? (neo/find-by-props
                  (users/get-user-node user-id)
                  :itest
                  {:id test-id}))))

(defn get-itest-node [user-id test-id]
  "Get itest node"
    (first (neo/find-by-props
            (users/get-user-node user-id)
            :itest
            {:id test-id})))

(defn get-itest [user-id test-id]
  "Get itest"
    (when-let [result (get-itest-node user-id test-id)]
      (neo/prop result)))

(defn get-all-users-itest-ids [user-id]
        (doall
         (map #(:id (neo/prop %))
              (neo/traverse (users/get-user-node user-id)
                            neo/breadth-first
                            (neo/depth-of 1)
                            neo/all-but-start
                            {:itest neo/outgoing}))))      

(defn add-itest! [user-id prop]
  "Add new itest, returning its id"
  (io!)
    (neo/with-tx
      ;; create itest
      (let [itest (neo/create-child! (users/get-user-node user-id) :itest prop)]
        (neo/create-relationship! (node-itests) :itest itest)
        ;; create iquestions
        (doseq [q (:questions prop)]
          (let [qprop (questions/instantiate-question q)
                iquestion (neo/create-child! itest :iquestion qprop)]
            (neo/create-relationship! (node-iquestions)
                                      :iquestion iquestion))))
      prop))

(defn- submit-question-node! [question-node prop]
  "Submit iquestion"
  (io!)
    (neo/with-tx
      (let [question-prop (neo/prop question-node)]
        (neo/set-properties! question-node {:answer ((keyword (:id question-prop)) prop)}))))

(defn submit-itest! [user-id test-id prop]
  "Submit itest"
  (io!)
    (neo/with-tx
      ;; find itest
      (let [itest (get-itest-node user-id test-id)]
        ;; update test
        (neo/set-properties! itest {:finished (str (clj-time/now))})
        ;; fill iquestions
        (doseq [q (neo/traverse itest
                                neo/breadth-first
                                (neo/depth-of 1)
                                neo/all-but-start
                                {:iquestion neo/outgoing})]
          (submit-question-node! q prop)))))
