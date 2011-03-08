;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.iquestion
  "Handle question instances"
  (:refer-clojure :exclude [get])
  (:require [borneo.core :as neo]
            [dredd.data.itest :as itest]))

;; iQuestions

(defn get-node [user-id test-id question-id]
  "Get iquestion"
  (when-let [node (itest/get-node user-id test-id)]
    (first (neo/traverse node {:id question-id} :iquestion))))

(defn get [user-id test-id question-id]
  "Get iquestion"
    (when-let [node (get-node user-id test-id question-id)]
      (neo/prop node)))

(defn rank! [user-id test-id question-id result comment]
  (let [node (get-node user-id test-id question-id)]
    (when node
      (neo/set-props! node {:result result :comment comment}))))
