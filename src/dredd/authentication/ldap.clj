(ns dredd.authentication.ldap
  "Simple LDAP SSL Authentication"
  (:require [clj-ldap.client :as ldap]
            [dredd.local-settings]))

;; Implementation details

(def ^{:private true
       :doc "Where to look for default LDAP settings map"}
  default-ldap-settings dredd.local-settings/ldap)

;; Public API

(defn authenticate-user
  "Try to login directly as user. If successful, return its attributes,
  nil otherwise. bind-dn is constructed with user-to-bind-dn fn from
  ldap-settings map"
  ([user password]
     (authenticate-user default-ldap-settings user password nil))
  ([user password attributes]
     (authenticate-user default-ldap-settings user password attributes))
  ([ldap-settings user password attributes]
     (try
       (let [dn ((:user-to-bind-dn ldap-settings) user)
             c (ldap/connect (merge ldap-settings {:bind-dn dn
                                                   :password password}))]
         (ldap/get c dn attributes))
       (catch Exception _ nil))))

;; Examples

(comment

  (authenticate-user "user" "password" [:sn :mail :cn :givenName])

  )
