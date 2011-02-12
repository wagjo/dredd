(ns dredd.core
  "App management"
  (:use compojure.core
        ring.util.response
        [hiccup core page-helpers form-helpers])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [dredd.neo4j :as neo]
            [dredd.authentication.ldap :as ldap]
            [dredd.local-settings]
            [dredd.db :as db]
            [dredd.tests :as tests]
            [ring.adapter.jetty :as adapter])
  (:import [clojure.lang ILookup IFn]))

;; Implementation details

(defn- prihlas-uzivatela! [username password]
  "Authenticate user and add it to database if not in it yet. Return user id"
  (io!)
  (when-let [auth-user (ldap/authenticate-user username password
                                               [:uid :givenName :sn :cn :mail])]
    (let [id (db/get-user-id auth-user)]
      (when-not (db/get-user id)
        (db/add-user! auth-user))
      id)))

(defn- login-form []
  (form-to [:post "login"]
           [:p "Meno" (text-field :username "")]
           [:p "Heslo" (password-field :password "")]
           (submit-button "Prihlas sa")))

(defn- menu-prihlaseneho [id]
  (let [user (db/get-user id)]
    [:p
     (db/get-user-name user) " | "
     [:a {:href "cvicenie"} "Vybrat cvicenie"] " | "
     [:a {:href "hodnotenie"} "Hodnotenie vasej pripravenosti"] " | "     
     [:a {:href "logout"} "Odhlasenie"]]))

(defn- main-page [{:keys [id message]}]
  (html
   (html5
    [:body
     [:h1 "Zistovanie pripravenosti studentov na cvicenia z predmetu Programovanie"]
     (when message [:p [:b message]])
     (if id
       (menu-prihlaseneho id)
       (login-form))])))

(defn- login! [username password]
  (io!)
  (let [id (prihlas-uzivatela! username password)]
    (-> (redirect "main")
        (assoc :session (if id
                          {:id id}
                          {:message "Nespravne meno alebo heslo!"})))))

(defn- logout []
  (-> (redirect "main")
      (assoc :session nil)))

(defn- can-take-test [user-id test-id]
  (let [itest (db/get-itest user-id test-id)]
    (or (nil? itest) (not (:finished itest)))))

(defn- ovladanie-testu [id t]
  (when (can-take-test id t)
    [:a {:href (str "test/" (:id t))} "Vybrat"]))

(defn- list-tests [id]
  (html
   (html5
    [:body
     [:h1 "Zoznam cviceni"]
     (map
      (fn [t] [:p (:name t) " | " (ovladanie-testu id (:id t))])
      tests/tests)])))

(defn- print-question-active [q]
  [:div :hr
   [:p "Otazka c." (:id q)]
   [:p (:name q)]
   [:p (:text q)]])

(defn- take-test [{:keys [id]} test-id]
  #_(let [itest (or (db/get-itest id test-id)
                   (db/add-itest id (db/get-test test-id)))
        user (db/get-user id)]
    (html
     (html5
      [:body
       [:h1 (:name itest)]
       (if id
         (if (:finished itest)
           [:p "Uz ste test vykonali!"]
           (map print-question-active itest))
         [:p "You are not logged in!"])]))))

(defmacro with-user [id & body]
  `(if ~id
     (do ~@body)
     {:status  403
      :headers {}
      :body    ""}))

;; Page layout

(defroutes main-routes
  (GET "/" []
       (redirect "/prog/main"))
  (GET "/main" {session :session}
       (main-page session))
  (GET "/cvicenie" {{id :id} :session}
       (with-user id
         (list-tests id)))
  (GET "/test/:test-id" {:keys [session route-params] :as r}
       (take-test session (:test-id route-params)))
  ;; TODO hodnotenie
  (GET "/logout" []
       (logout))
  (POST "/login" [username password]
        (login! username password))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))

;; Server management

(defonce *server* nil)

(defn start-server []
  (db/init!)
  (let [server (adapter/run-jetty app {:port 3000 :join? false})]
    (alter-var-root #'*server* (fn [_] server))))

(defn stop-server []
  (.stop *server*))

(defn start-and-wait []
  (db/init!)
  (adapter/run-jetty app {:port 3000}))

;; Examples

(comment

  (start-server)

  (stop-server)
  
  (neo/with-neo
    (neo/with-tx
      (let [unode (neo/go (neo/root) :users)
            result (neo/traverse
                    unode
                    neo/breadth-first
                    (neo/depth-of 1)
                    neo/all-but-start
                    {:user neo/outgoing})]
        (.getPropertyKeys (first result)))))

  )
