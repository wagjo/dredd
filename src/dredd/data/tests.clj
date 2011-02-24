(ns dredd.data.tests
  "List of tests"
  (:use [clojure.contrib.seq-utils :only [find-first]]))

(def tests
  [{:id "t1"
    :name "Cvicenie 1"
    :questions ["q1" "q2"]}
   {:id "t2"
    :name "Cvicenie 2"
    :questions ["q3" "q4"]}])

(defn get-test [id]
  (find-first #(= id (:id %)) tests))
