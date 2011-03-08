;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.authentication.ldap
  "Simple LDAP SSL Authentication."
  (:require [clj-ldap.client :as ldap]
            [dredd.local-settings :as local-settings]))

;; Public API

(defn get-user
  "Try to login directly as user. If successful, return its attributes,
  nil otherwise. bind-dn is constructed with user-to-bind-dn fn from
  ldap-settings map"
  ([user password]
     (get-user local-settings/ldap user password nil))
  ([user password attributes]
     (get-user local-settings/ldap user password attributes))
  ([ldap-settings user password attributes]
     (try
       (let [dn ((:user-to-bind-dn ldap-settings) user)
             c (ldap/connect (merge ldap-settings {:bind-dn dn
                                                   :password password}))]
         (ldap/get c dn attributes))
       (catch Exception _ nil))))

(defn auth-user
  "Authenticate ldap user. Returns his id or nil."
  [username password]
  (:uid (get-user username password [:uid])))

;; Examples

(comment

  (authenticate-user "user" "password" [:sn :mail :cn :givenName])

)
