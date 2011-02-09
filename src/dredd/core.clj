(ns dredd.core
  (:require [dredd.neo4j :as neo]
            [dredd.local-settings])
  (:import [clojure.lang ILookup IFn]))

(deftype A [x]
  ILookup
  (valAt [this key] x)
  (valAt [this key notFound] (str x " " notFound))
  IFn
  (invoke [this] nil)
  (invoke [this key] x)
  Object
  (toString [this] x))

#_(neo/with-neo
  2)

;;; Create a root for customers and add a customer.
#_(with-tx
  (let [customer-root (new-node) 
        bob (new-node)]
    (relate (top-node) :customers customer-root)
    (relate customer-root :customer bob)
    (properties bob {"name" "Bob"
                     "age" 30
                     "id" "C12345"})))

