;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.iquestion
  "Handle question instances"
  (:refer-clojure :exclude [get])
  (:require [borneo.core :as neo]
            [dredd.data.itest :as itest]
            [dredd.data.questions :as questions]))

;;;; Implementation details

(defn- node-iquestions []
  "Gets iquestions node."
  (neo/walk (neo/root) :iquestions))

;;;; Public API

(defn exists?
  "Returns true if given iquestion node exists."
  [user-id test-id question-id]
  (when-let [node (itest/get-node user-id test-id)]
    (not (empty? (neo/traverse node {:id question-id} :iquestion)))))

(defn get-node [user-id test-id question-id]
  "Gets iquestion node."
  (when-let [node (itest/get-node user-id test-id)]
    (first (neo/traverse node {:id question-id} :iquestion))))

(defn get [user-id test-id question-id]
  "Gets iquestion props."
    (when-let [node (get-node user-id test-id question-id)]
      (neo/props node)))

(defn add! [user-id test-id question-id]
  "Adds new iquestion to existing itest."
  (let [parent (itest/get-node user-id test-id)
        node (neo/create-child! parent :iquestion (questions/instantiate question-id))]
    (neo/create-rel! (node-iquestions) :iquestion node)))

(defn submit-answer! [user-id test-id question-id answer]
  "Submit answer."
  (let [node (get-node user-id test-id question-id)]
    (neo/set-prop! node :answer answer)))

(defn rank! [user-id test-id question-id result comment]
  "Rank submitted question."
  (let [node (get-node user-id test-id question-id)]
    (neo/set-props! node {:result result :comment comment})))
