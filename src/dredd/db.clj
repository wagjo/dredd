(ns dredd.db
  "Database stuff"
  (:require [dredd.neo4j :as neo]
            [dredd.tests :as tests]
            [dredd.questions :as questions]))

;; Helper Vars

(defn- node-users []
  "Get users node"
  (neo/go (neo/root) :users))

(defn- node-itests []
  "Get itests node"
  (neo/go (neo/root) :itests))

(defn- node-iquestions []
  "Get iquestions node"
  (neo/go (neo/root) :iquestions))

;; Public API

(defn init! []
  "Init neo database. Call this once when app starts"
  (io!)
  (neo/with-neo
    (neo/with-tx
      (doseq [rel-type [:users :itests :iquestions :tests :questions]]
        (when-not (neo/rel? (neo/root) rel-type)
          (neo/create-child! rel-type nil))))))

;; Users

(defn get-user-node [id]
  "Get user node"
  (neo/with-neo
    (first (neo/find-by-props
            (node-users)
            :user
            {:uid id}))))

(defn get-user [id]
  "Get user properties"
  (neo/with-neo
    (when-let [result (get-user-node id)]
      (neo/prop result))))

(def get-user-name :cn)

(def get-user-id :uid)

(defn add-user! [props]
  "Add user to the database"
  (io!)
  (neo/with-neo
    (neo/with-tx
      (neo/create-child! (node-users) :user props))))

;; iTests

(defn itest? [user-id test-id]
  "Determine whether user has instance of some test"
  (neo/with-neo
    (not (empty? (neo/find-by-props
                  (get-user-node user-id)
                  :itest
                  {:id test-id})))))

(defn get-itest-node [user-id test-id]
  "Get itest node"
  (neo/with-neo
    (first (neo/find-by-props
            (get-user-node user-id)
            :itest
            {:id test-id}))))

(defn get-itest [user-id test-id]
  "Get itest"
  (neo/with-neo
    (when-let [result (get-itest-node user-id test-id)]
      (neo/prop result))))

(defn add-itest! [user-id prop]
  "Add new itest, returning its id"
  (io!)
  (neo/with-neo
    (neo/with-tx
      ;; create itest
      (let [itest (neo/create-child! (get-user-node user-id) :itest prop)]
        (neo/create-relationship! (node-itests) :itest itest)
        ;; create iquestions
        (doseq [q (:questions prop)]
          (let [qprop (questions/instantiate-question q)
                iquestion (neo/create-child! itest :iquestion qprop)]
            (neo/create-relationship! (node-iquestions)
                                      :iquestion iquestion))))
      (:id prop))))

;; iQuestions

(defn get-iquestion [user-id test-id question-id]
  "Get iquestion"
  (neo/with-neo
    (let [result (first (neo/find-by-props
                         (get-itest-node user-id test-id)
                         :iquestion
                         {:id question-id}))]
      (when result
        (neo/prop result)))))
