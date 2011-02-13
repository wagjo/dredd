(ns dredd.data.users
  "Database stuff"
  (:require [dredd.db-adapter.neo4j :as neo]
            [dredd.authentication.ldap :as ldap]
            [dredd.authentication.admin :as admin]))

;; Helper Vars

(defn- node-users []
  "Get users node"
  (neo/go (neo/root) :users))

;; Users

(defn admin? [id]
  (= "admin" id))

(def get-user-name :cn)

(def get-user-id :uid)

(defn get-user-node [id]
  "Get user node"
  (neo/with-neo
    (first (neo/find-by-props
            (node-users)
            :user
            {:uid id}))))

(defn get-user [id]
  "Get user properties"
  (if (= "admin" id)
    {:cn "Administrator"
     :uid "admin"
     :admin true}
    (neo/with-neo
      (when-let [result (get-user-node id)]
        (neo/prop result)))))

(defn get-all-user-ids []
  (neo/with-neo
    (doall
     (map #(get-user-id (neo/prop %))
          (neo/traverse (node-users)
                        neo/breadth-first
                        (neo/depth-of 1)
                        neo/all-but-start
                        {:user neo/outgoing})))))

(defn add-user! [props]
  "Add user to the database"
  (io!)
  (neo/with-neo
    (neo/with-tx
      (neo/create-child! (node-users) :user props))))

(defn login-user! [username password]
  "Authenticate user and add it to database if not in it yet. Return user-id"
  (io!)
  ;; first see if is admin
  (if (admin/authenticate-user username password)
    "admin"
    (if (and (= "guest" username)
             (= "guest" password))
      ;; guest account
      (let [auth-user {:uid "guest"
                       :givenName "Jozko"
                       :sn "Mrkvicka"
                       :cn "Jozko Mrkvicka"
                       :mail "mail@mail.com"}]
        (let [user-id (get-user-id auth-user)]
          (when-not (get-user user-id)
            (add-user! auth-user))
          user-id))      
      ;; log in as mortal user
      (when-let [auth-user (ldap/authenticate-user username password
                                                   [:uid :givenName :sn :cn :mail])]
        (let [user-id (get-user-id auth-user)]
          (when-not (get-user user-id)
            (add-user! auth-user))
          user-id)))))

(defn set-user-group! [user-id group]
  (when (and group (not (empty? group)))
    (neo/with-neo
      (neo/with-tx
        (neo/set-properties! (get-user-node user-id) {:group group})))))
