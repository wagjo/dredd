(ns dredd.ldap
  "LDAP SSL Authentication"
  (:require [dredd.local-settings])
  (:import [com.unboundid.ldap.sdk LDAPConnection SearchScope LDAPException]
           [com.unboundid.util.ssl SSLUtil TrustAllTrustManager]
           [javax.net.ssl SSLSocketFactory]))

;; Helper functions

(defn- ssl-connect [host port user-dn password]
  "Open LDAP SSL Connection. Do not forget to call close"
  (let [u (SSLUtil. (TrustAllTrustManager.))
        sf (.createSSLSocketFactory u)]
    (LDAPConnection. sf host port user-dn password)))

(defn- entry-to-map [entry]
  "Convert Search Result Entry into a simple map"
  (reduce #(assoc %1 (keyword (.getName %2)) (.getValue %2))
          {:dn (.getDN entry)}
          (.getAttributes entry)))

;; Public API

(defn get-entry [{:keys [host port admin-dn admin-pass base-dn user-login]} user attributes]
  "Search for a LDAP entry"
  (let [admin-connection (ssl-connect host port admin-dn admin-pass)
        search-filter (str "(" user-login "=" user ")")
        attributes-array (into-array String (map name attributes))
        search-result (.search admin-connection base-dn SearchScope/SUB search-filter attributes-array)
        result (->> search-result
                    .getSearchEntries
                    first
                    entry-to-map)]
    (.close admin-connection)
    result))

(defn auth-user [{:keys [host port] :as settings} user password]
  "Moodle-like user auth. First get user dn and then try to bind as this user"
  (try
    (let [user-dn (:dn (get-entry settings user [:foo]))
          user-connection (ssl-connect host port user-dn password)]
      (.close user-connection)
      true)
    (catch LDAPException _ false)))

;; Examples

(comment

  (get-entry dredd.local-settings/ldap "jw817dk" nil)

  )
