;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.authentication
  "Handle user authentication."
  (:require [dredd.local-settings :as settings]
            [dredd.ldap :as ldap]))

;;;; Implementation details

(defn- auth-admin
  "Authenticate admin. Returns his id or nil."
  [username password]
  (and (= username (:username settings/admin))
       (= password (:password settings/admin))
       (:uid settings/admin)))

(defn- auth-guest
  "Authenticate guest. Returns his id or nil."
  [username password]
  (and (= username (:username settings/guest))
       (= password (:password settings/guest))
       (:uid settings/guest)))

;;;; Public API

(defn auth-user
  "Authenticate user and returns his id or nil."
  [username password]
  (some #(% username password) [auth-admin auth-guest ldap/auth-user]))
