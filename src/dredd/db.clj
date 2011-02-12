(ns dredd.db
  "Database stuff"
  (:require [dredd.neo4j :as neo]))

;; Helper Vars

(defn- node-users []
  "Get users node"
  (neo/go (neo/root) :users))

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

(defn get-user [id]
  "Get user from database and return its attributes"
  (neo/with-neo
    (let [result (first (neo/find-by-props
                         (node-users)
                         :user
                         {:uid id}))]
      (when result
        (neo/prop result)))))

(def get-user-name :cn)

(def get-user-id :uid)

(defn add-user! [props]
  "Add user to the database"
  (io!)
  (neo/with-neo
    (neo/with-tx
      (neo/create-child! (node-users) :user props))))
