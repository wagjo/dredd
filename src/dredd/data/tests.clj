(ns dredd.data.tests
  "List of tests"
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
    :questions ["q7" "q8"]}
   {:id "t5"
    :name "Cvicenie 5"
    :questions ["q9" "q10"]}
   {:id "t6"
    :name "Cvicenie 6"
    :questions ["q11" "q12"]}
   {:id "t7"
    :name "Cvicenie 7"
    :questions ["q13" "q14"]}   
   {:id "t8"
    :name "Cvicenie 8"
    :questions ["q21" "q22"]}   
   {:id "t9"
    :name "Cvicenie 9"
    :questions ["q23"]}
   {:id "t10"
    :name "Cvicenie 10"
    :questions ["q24" "q25"]}
   {:id "t11"
    :name "Cvicenie 11"
    :questions ["q26" "q27"]}
    ])

(defn get-test [id]
  (find-first #(= id (:id %)) tests))
