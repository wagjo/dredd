;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.user
  "Handle users."
  (:refer-clojure :exclude [get])  
  (:require [borneo.core :as neo]
            [dredd.local-settings :as settings]
            [dredd.authentication :as authentication]
            [dredd.ldap :as ldap]))

;;;; Implementation details

(defn- node-users
  "Get users node."
  []
  (neo/walk (neo/root) :users))

(defn- get-special-user-node
  "Returns special user node, if user-id is correct, or nil."
  [user-type user-id]
  (let [node (neo/walk (neo/root) user-type)]
    (when (= (neo/prop node :uid) user-id)
      node)))

(defn- get-user-node
  "Returns user node."
  [user-id]
  (first (neo/traverse (node-users) {:uid user-id} :user)))

(declare get-node)

(defn- add!
  "Add user to the database, if he not exists yet."
  [props]
  (io!)
  (when-not (get-node (:uid props))
    (neo/create-child! (node-users) :user props)))

;;;; Public API

(defn admin?
  "Returns true if user-id belong to the admin user."
  [user-id]
  (= user-id settings/admin))

(def authorization-table
     ;; operation role pairs, nil means no special role needed
     {:user-ops nil
      :admin-ops :admin})

(defn authorized?
  "Check if user is authorized to perform operation of given type."
  [user-id operation]
  (let [user-roles (set (flatten [(:role (get user-id))]))
        required-roles (set (flatten [(authorization-table operation)]))]
    (not (empty? (clojure.set/intersection user-roles required-roles)))))

(defn get-node
  "Gets user node."
  [user-id]
  (some #(% user-id) [(partial get-special-user-node :admin)
                      (partial get-special-user-node :guest)
                      get-user-node]))

(defn get
  "Gets user properties."
  [user-id]
  (when-let [node (get-node user-id)]
    (neo/props node)))

(defn get-all-nodes
  "Returns all user nodes."
  []
  (neo/traverse (node-users) :user))

(defn get-all
  "Return all user ids."
  []
  (map #(neo/prop % :uid) (get-all-nodes)))

(defn login! [username password]
  "Authenticates user and add it to database if not in it yet. Returns user id."
  (io!)
  (when-let [user-id (authentication/auth-user username password)]
    ;; if user has logged in for the first time, create his node
    (when-not (get-node user-id)
      (add! (ldap/get-user username password [:uid :cn :sn :givenName :mail])))
    user-id))

(defn set-group! [user-id group]
  "Sets or removes users group."
  (neo/set-prop! (get-node user-id) :group group))
