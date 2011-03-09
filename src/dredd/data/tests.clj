;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.tests
  "List of tests."
  (:refer-clojure :exclude [get])
  (:use [clojure.contrib.seq-utils :only [find-first]]))

(def tests
  [{:id "t1"
    :name "Cvicenie 1"
    :questions ["q1" "q2"]}
   {:id "t2"
    :name "Cvicenie 2"
    :questions ["q3" "q4"]}
   {:id "t3"
    :name "Cvicenie 3"
    :questions ["q5" "q6"]}
   {:id "t4"
    :name "Cvicenie 4"
    :questions ["q7" "q8"]}])

(defn get [id]
  (find-first #(= id (:id %)) tests))
