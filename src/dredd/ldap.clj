(ns dredd.ldap
  "LDAP SSL Authentication"
  (:require [dredd.local-settings])
  (:import [com.unboundid.ldap.sdk LDAPConnection SearchScope LDAPException]
           [com.unboundid.util.ssl SSLUtil TrustAllTrustManager]
           [javax.net.ssl SSLSocketFactory]))

;; Helper functions

(defn- ssl-connect [host port user-dn password]
  "Open LDAP SSL Connection. Do not forget to call close"
  (let [u (SSLUtil. (TrustAllTrustManager.)) ;; trust all certificates
        sf (.createSSLSocketFactory u)]
    (LDAPConnection. sf host port user-dn password)))

(defn- entry-to-map [entry]
  "Convert Search Result Entry into a simple map"
  (reduce #(assoc %1 (keyword (.getName %2)) (.getValue %2))
          {:dn (.getDN entry)} ;; each entry will have at least its dn
          (.getAttributes entry)))

;; Public API

(defn get-entry
  "Search for a LDAP entry"
  ([user attributes]
     (get-entry dredd.local-settings/ldap user attributes))
  ([{:keys [host port admin-dn admin-pass base-dn user-login]} user attributes]
     (with-open [admin-connection (ssl-connect host port admin-dn admin-pass)]
       (let [search-filter (str "(" user-login "=" user ")")
             attributes-array (into-array String (map name attributes))]
         (->> (.search admin-connection base-dn SearchScope/SUB search-filter attributes-array)
              .getSearchEntries
              first ;; we are interested only in the first result
              entry-to-map)))))

(defn auth-user
  "Moodle-like user auth. First get user dn and then try to bind as this user"
  ([user password]
     (auth-user dredd.local-settings/ldap user password))
  ([{:keys [host port] :as settings} user password]
     (try
       (let [user-dn (:dn (get-entry settings user [:foo]))]
         (with-open [user-connection (ssl-connect host port user-dn password)]
           ;; if bind was successful, user is authenticated
           true))
       (catch LDAPException _ false))))

;; Examples

(comment

  (get-entry "jw817dk" nil)

  )
