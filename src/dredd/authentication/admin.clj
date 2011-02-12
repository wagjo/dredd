(ns dredd.authentication.admin
  "How admin is authenticated in dredd"
  (:require [dredd.local-settings]))

;; Implementation details

(def ^{:private true
       :doc "Where to look for default admin settings map"}
  default-admin-settings dredd.local-settings/admin)

;; Public API

(defn authenticate-user
  "Just see if password matches with admin password in settings"
  ([username? password?]
     (authenticate-user default-admin-settings username? password?))
  ([{:keys [username password]} username? password?]
     (and (= username username?)
          (= password password?))))

;; Examples

(comment

  (authenticate-user "admin" "admin")

  )
