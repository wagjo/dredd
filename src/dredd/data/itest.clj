;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.itest
  "Handle test instances."
  (:refer-clojure :exclude [get])
  (:require [borneo.core :as neo]
            [clj-time.core :as clj-time]            
            [dredd.data.user :as user]))

;;;; Implementation details

(defn- node-itests []
  "Gets itests node."
  (neo/walk (neo/root) :itests))

;;;; Public API

(defn get-node [user-id test-id]
  "Gets itest node."
    (first (neo/traverse (user/get-node user-id)
                         {:id test-id} :itest)))

(defn get [user-id test-id]
  "Gets itest."
  (when-let [node (get-node user-id test-id)]
    (neo/props node)))

(defn itest? [user-id test-id]
  "Determine whether user has instance of some test."
  (not (nil? (get-node user-id test-id))))

(defn get-user-itest-ids [user-id]
  "Returns ids of all users itests."
  (doall
   (map #(neo/prop % :id)
        (neo/traverse (user/get-node user-id) :itest))))

(defn add! [user-id props]
  "Adds new itest, returning its id. You have to add iquestions then."
  (let [itest (neo/create-child! (user/get-node user-id) :itest props)]
    (neo/create-rel! (node-itests) :itest itest)
    (:id props)))

(defn finish! [user-id test-id]
  "Sets itest state to finished."
  (let [node (get-node user-id test-id)]
    (neo/set-prop! node :finished (str (clj-time/now)))))

