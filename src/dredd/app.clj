(ns dredd.app
  "App management"
  (:use compojure.core
        ring.util.response
        [hiccup core page-helpers form-helpers])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [dredd.data.tests :as tests]
            [dredd.data.itests :as itests]
            [dredd.data.users :as users]
            [dredd.local-settings :as local-settings]
            [dredd.data.iquestions :as iquestions]))

;; Implementation details

;; Main

(defn- login-form []
  (form-to [:post "login"]
           [:p "Username: " (text-field :username "")]
           [:p "Password: " (password-field :password "")]
           (submit-button "Log in")))

(defn- user-menu [user-id]
  (let [user (users/get-user user-id)]
    [:p
     (users/get-user-name user) " | "
     [:a {:href "choose"} "Vybrat cvicenie"] " | " ;; NOTE: localization
     [:a {:href "logout"} "Log out"]]))

(defn- main-page [user-id message]
  (html
   (html5
    [:body
     [:h1 "Zistovanie pripravenosti studentov na cvicenia z predmetu Programovanie"] ;; NOTE: localization
     (when message [:p [:b message]])
     (if user-id
       (user-menu user-id)
       (login-form))])))

;; Login

(defn- login-page! [username password]
  (io!)
  (let [user-id (users/login-user! username password)]
    (-> (redirect "main")
        (assoc :session (if user-id
                          {:user-id user-id}
                          {:message "Wrong username or password"})))))

;; Logout

(defn- logout-page []
  (-> (redirect "main")
      (assoc :session nil)))

;; Choosing test

(defn- can-take-test [user-id test-id]
  (let [itest (itests/get-itest user-id test-id)]
    (or (nil? itest) (not (:finished itest)))))

(defn- show-controls [user-id test-id]
  (if (users/admin? user-id)
    [:a {:href (str "admin/" test-id)} "Administracia"]
    (if (can-take-test user-id test-id)
      [:a {:href (str "test/" test-id)} "Otvor"]
      [:a {:href (str "view/" test-id)} "Pozri vysledky"])))

(defn- choose-page [user-id]
  (html
   (html5
    [:body
     [:h1 "Vyber si cvicenie"] ;; NOTE: Localization
     (map
      (fn [t] [:p (:name t) " | " (show-controls user-id (:id t))])
      tests/tests)])))

;; Taking test

(defn- print-iquestion [& ids]
  (let [q (apply iquestions/get-iquestion ids)]
    [:div 
     [:p [:b "Question " (:id q) ": "] (:name q)]
     [:p [:i (:text q)]]
     [:textarea {:rows "15" :cols "80" :name (:id q)} ""]]))

(defn- take-test-page [user-id test-id]
  (if-not (can-take-test user-id test-id)
    ;; user has already finished the test
    {:status  403
     :headers {}
     :body    "You have already finished this test"}
    ;; ready to take a test
    (let [itest (or (itests/get-itest user-id test-id)
                    (itests/add-itest! user-id (tests/get-test test-id)))
          user (users/get-user user-id)]
      (html
       (html5
        [:body
         [:h1 (:name itest)]
         (form-to [:post "../submit-test"]
                  (hidden-field :test-id test-id)
                  [:hr]
                  (map (partial print-iquestion user-id test-id) (:questions itest))
                  [:hr]
                  (submit-button "Odoslat a Ukoncit"))]))))) ;; NOTE: Localization

;; Viewing test

(defn- view-iquestion [& ids]
  (let [q (apply iquestions/get-iquestion ids)]
    [:div 
     [:p [:b "Question " (:id q) ": "] (:name q)]
     [:p [:i (:text q)]]
     [:p [:i "Your answer: "]]
     [:p [:pre (h (:answer q))]]
     [:p [:i "Result: "] (:result q)]]))

(defn- view-test-page [user-id test-id]
  (let [itest (itests/get-itest user-id test-id)
        user (users/get-user user-id)]
    (html
     (html5
      [:body
       [:h1 (:name itest)]
       [:hr]
       (map (partial view-iquestion user-id test-id) (:questions itest))
       [:hr]]))))

;; Submitting test

(defn- submit-test! [user-id test-id params]
  (io!)
  ;; TODO: only if not finished yet
  (itests/submit-itest! user-id test-id params)
  (-> (redirect "main")
      (assoc :session {:user-id user-id
                       :message "Uspesne odoslane!"})))

;; Administrator interface

(defn- admin-user-test [user-id test-id]
  (let [user (users/get-user user-id)
        itest (itests/get-itest user-id test-id)]
    [:div
     [:hr]
     [:p (users/get-user-name user) " (" user-id ")"]
     (if (:finished itest)
       [:p "Test finished at " (:finished itest)]
       [:p "Test NOT finished"])
     (map (partial view-iquestion user-id test-id) (:questions itest))]))

(defn- admin-test-page [test-id]
  (let [user-ids (users/get-all-user-ids)]
    (html
     (html5
      [:body
       [:h1 "Administracia"]
       (map #(admin-user-test % test-id) user-ids)]))))

;; Middleware

(defmacro with-user [user-id & body]
  `(if ~user-id
     (do ~@body)
     {:status  403
      :headers {}
      :body    "You must be logged in to view this page!"}))

(defmacro with-admin [user-id & body]
  `(if (users/admin? ~user-id)
     (do ~@body)
     {:status  403
      :headers {}
      :body    "You must be administrator to view this page"}))

(defmacro with-test [test-id & body]
  `(if (tests/get-test ~test-id)
     (do ~@body)
     {:status  403
      :headers {}
      :body    "There is no such test!"}))

;; Page layout

(defroutes main-routes
  (GET "/" []
       (redirect (str (:base-url local-settings/app) "/main")))
  (GET "/main" {{:keys [user-id message]} :session}
       (main-page user-id message))
  (GET "/choose" {{user-id :user-id} :session}
       (with-user user-id
         (choose-page user-id)))
  (GET "/test/:test-id" {{user-id :user-id} :session {test-id :test-id} :route-params}
       (with-user user-id
         (with-test test-id
           (take-test-page user-id test-id))))
  (GET "/admin/:test-id" {{user-id :user-id} :session {test-id :test-id} :route-params}
       (with-admin user-id
         (with-test test-id
           (admin-test-page test-id))))  
  (GET "/view/:test-id" {{user-id :user-id} :session {test-id :test-id} :route-params}
       (with-user user-id
         (with-test test-id
           (view-test-page user-id test-id))))
  (POST "/submit-test" {{user-id :user-id} :session {test-id :test-id :as params} :params}
       (with-user user-id
         (with-test test-id
           (submit-test! user-id test-id params))))  
  ;; TODO hodnotenie
  (GET "/logout" []
       (logout-page))
  (POST "/login" [username password]
        (login-page! username password))
  (route/resources "/")
  (route/not-found "Page not found"))

;; Main App handler

(def app
  (handler/site main-routes))

;; Examples

(comment


  
)
