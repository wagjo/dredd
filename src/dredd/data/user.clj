;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.user
  "Handle users."
  (:refer-clojure :exclude [get])  
  (:require [borneo.core :as neo]
            [dredd.local-settings :as local-settings]
            [dredd.authentication.ldap :as ldap]))

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

;; Users

(def get-name :cn)

(def get-id :uid)

(defn get-node
  "Get user node."
  [user-id]
  (some #(% user-id) [(partial get-special-user-node :admin)
                      (partial get-special-user-node :guest)
                      get-user-node]))

(defn get
  "Get user properties."
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
  (map #(neo/prop % get-id) (get-all-nodes)))

(defn add!
  "Add user to the database, if he not exists yet."
  [props]
  (io!)
  (when-not (get-node (get-id props))
    (neo/create-child! (node-users) :user props)))

(defn- auth-admin
  "Authenticate admin. Returns his id or nil."
  [username password]
  (and (= username (:username local-settings/admin))
       (= password (:password local-settings/admin))
       (:uid local-settings/admin)))

(defn- auth-guest
  "Authenticate guest. Returns his id or nil."
  [username password]
  (and (= username (:username local-settings/guest))
       (= password (:password local-settings/guest))
       (:uid local-settings/guest)))

(defn- auth-user
  "Authenticate user and returns his id or nil."
  [username password]
  (some #(% username password) [auth-admin auth-guest ldap/auth-user]))

(defn login! [username password]
  "Authenticates user and add it to database if not in it yet. Returns user id."
  (io!)
  (when-let [user-id (auth-user username password)]
    ;; if user has logged in for the first time, create his node
    (when-not (get-node user-id)
      (add! (ldap/get-user username password [:uid :cn :sn :givenName :mail])))
    user-id))

(defn set-group! [user-id group]
  "Sets or removes users group."
  (neo/set-prop! (get-node user-id) :group group))
