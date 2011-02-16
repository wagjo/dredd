(ns dredd.db-adapter.neo4j
  "Neo4j wrapper, forked from hgavin/clojure-neo4j"
  (:require [dredd.local-settings])
  (:import (org.neo4j.graphdb Direction
                              Node
                              NotFoundException
                              NotInTransactionException
                              PropertyContainer
                              Relationship
                              RelationshipType
                              ReturnableEvaluator
                              StopEvaluator
                              Transaction
                              TraversalPosition
                              Traverser
                              Traverser$Order)
	   (org.neo4j.kernel EmbeddedGraphDatabase)))

;; Helper functions

(def *neo-db* nil)

(def *root* nil)

(defn- name-or-str [x]
  (if (keyword? x) 
    (name x) 
    (str x)))

;; Public API

(defn start-neo
  ([] (start-neo (:path dredd.local-settings/neo4j)))
  ([path] (let [n (EmbeddedGraphDatabase. path)]
            (alter-var-root #'*neo-db* (fn [_] n)))))

(defn stop-neo []
  (.shutdown *neo-db*))

(defmacro with-neo [path & body]
  "Establish a connection to the neo db.
  If path is not supplied, uses path from local_settings.clj
  Not good when mulitple threads try to access same database"
  (let [npath (if (string? path) path (:path dredd.local-settings/neo4j))
        nbody (if (string? path) body (cons path body))]
    `(if *neo-db*
      (do ~@nbody) ;; we are already in
      (binding [*neo-db* (EmbeddedGraphDatabase. ~npath)]
         (try
           ~@nbody
           (finally (.shutdown *neo-db*)))))))

(defmacro with-tx [& body]
  "Establish a transaction. If you do not want to commit it, throw an exception"
  `(let [tx# (.beginTx *neo-db*)]
     (try
       (let [val# (do ~@body)]
         (.success tx#)
         val#)
       (finally (.finish tx#)))))

(defn root []
  "Get top node"
  (.getReferenceNode *neo-db*))

(def both Direction/BOTH)

(def incoming Direction/INCOMING)

(def outgoing Direction/OUTGOING)

(defn relationship [n]
  (proxy [RelationshipType] []
    (name [] (name n))))

(defn rel?
  "Returns true if relathinship exists, false otherwise.
  Allowed arguments besides node:
  - none - test for any relationship
  - direction - any relationships with specified direction
  - type - relationship of specified type with any direction
  - direction type - relationship of specified type and of specified direction
  - type & types - relationship of any of specified types with any direction"
  ([node]
     (.hasRelationship node))
  ([node type-or-direction & types]
     (let [dir (if (= Direction (class type-or-direction))
                 type-or-direction
                 both)
           t (map relationship
                  (if (= Direction (class type-or-direction))
                    (do
                      ;; if multiple types, direction is both
                      (when (> (count types) 1) (throw (IllegalArgumentException.)))
                      types)
                    (cons type-or-direction types)))]
       (cond
        (empty? t) (.hasRelationship node dir)
        (= 1 (count t)) (.hasRelationship node (first t) dir)
        :else (.hasRelationship node (into-array RelationshipType t))))))

(defn go [node & types]
  "Walk through the graph by following specified relations"
  (let [rel-types (map relationship types)
        next-node (fn [node type]
                    (.getEndNode (.getSingleRelationship node type outgoing)))]
    (reduce next-node node rel-types)))

(defn array? [x] (-> x class .isArray))

(defn prop 
  "Return a map of properties."
  ([#^PropertyContainer c]
     (let [ks (.getPropertyKeys c)]
       (into {} (map (fn [k] [(keyword k)
                             (let [v (.getProperty c k)]
                               (if (array? v)
                                 (seq v)
                                 v))]) ks)))))

(defn set-properties! 
  "Set a map of properties."
  [#^PropertyContainer c props]
  (doseq [[k v] props]
    ;; TODO support primivives
    (.setProperty c (name-or-str k)
                  (if (coll? v)
                    (into-array String v)
                    (or v "")))))

(defn create-relationship! [#^Node from type #^Node to]
  "Create relationship of a supplied type between from and to"
  (io!)
  (.createRelationshipTo from to (relationship type)))

(defn create-node!
  "Create a new node"
  ([]
     (io!)
     (.createNode *neo-db*))
  ([props]
     (doto (create-node!)
       (set-properties! props))))

(defn create-child!
  "Creates a node that is a child of the specified parent node
  (or root node) along the specified relationship.
  props is a map that defines the properties of the node."
  ([type props]
     (create-child! (root) type props))
  ([node type props]
     (let [child (create-node! props)]
       (create-relationship! node type child)
       child)))

;; old code

(def *neo* nil)

(declare properties)

(def breadth-first Traverser$Order/BREADTH_FIRST)
(def depth-first   Traverser$Order/DEPTH_FIRST)

(defn depth-of 
  "Return a StopEvaluator for the given traversal depth."
  [d] 
  (if (== d 1) 
    StopEvaluator/DEPTH_ONE
    (proxy [StopEvaluator] []
      (isStopNode [#^TraversalPosition pos]
                  (== (.depth pos) d)))))

(def end-of-graph StopEvaluator/END_OF_GRAPH)

(def all ReturnableEvaluator/ALL)
(def all-but-start ReturnableEvaluator/ALL_BUT_START_NODE)



(defn new-node 
  ([] (.createNode *neo*))
  ([props] (let [node (new-node)]
             (properties node props)
             node)))

(defn relationship [#^Keyword n]
  (proxy [RelationshipType] []
    (name [] (name n))))

(defn relate [#^Node from #^Keyword type #^Node to]
  (.createRelationshipTo from to (relationship type)))

(defn return-if [f]
  "Creates a ReturnableEvaluator for use with a traverser. Takes a
  function of one argument. The function will be passed the current
  position to act on."
  (proxy [ReturnableEvaluator] []
    (isReturnableNode [#^TraversalPosition p] (f p))))

(defn stop-if [f]
  (proxy [StopEvaluator] []
    (isStopNode [#^TraversalPosition p] (f p))))

(defn property
  "DEPRECATED.  Prefer properties."
  {:deprecated "0.3.0"}
  ([#^PropertyContainer c key]
     (.getProperty c (name key)))
  ([#^PropertyContainer c key val]
     (.setProperty c (name-or-str key) val)))

(defn properties 
  "Return or set a map of properties."
  ([#^PropertyContainer c]
     (let [ks (.getPropertyKeys c)]
       (into {} (map (fn [k] [(keyword k) (.getProperty c k)]) ks))))
  ([#^PropertyContainer c props]
     (doseq [[k v] props]
       (.setProperty c (name-or-str k) (or v "")))
     nil))

(defn map-node
  "Takes a node and exposes its properties as a map. The node itself
  will be an entry in the map keyed with :node.  If a value is already
  at :node, it will be replaced by the refernece to the node itself."
  [#^Node n]
  (assoc (properties n) :node n))

(defn map-node-seq
  "Takes a sequence of nodes and creates a lazy sequence that converts
  each node to a map using map-node."
  [seq]
  (map #(with-tx (map-node %)) seq))

(defn update-mapped-node
  "Upates a node in the Neo4j database that has been mapped with
  map-node. Compares the values of the map to the values stored in the
  mapped node. Any values that have changed will be updated in the
  database."
  [mapped-node]
  (let [node (:node mapped-node)]
    (doseq [k (keys (properties node))]
      (when-not (= (get mapped-node k) (get node k))
	(.setProperty node (name-or-str k) (or (get mapped-node k) ""))))
    mapped-node))

;; Should cond-mapped have an :else?
(defmacro cond-mapped
  "Tests to see if n is a mapped node. If so, executes mappedexp. If n
  is a regular node, nodeexp is executed."
  [n mappedexp nodeexp]
  `(cond
    (map? ~n)
    ~mappedexp

    (isa? (class ~n) Node)
    ~nodeexp))

(defn new-child-node
  "Creates a node that is a child of the specified parent node along
  the specified relationship.  props is a map that defines the
  properties of the node."
  [parentnode relkey props]
  (cond-mapped parentnode
	       (new-child-node (:node parentnode) relkey props)
	       (let [node (new-node)]
		 (relate parentnode relkey node)
		 (properties node props)
		 node)))

(defn node-delete 
  "Delete the given node."
  [n]
  (cond-mapped n
	       (node-delete (:node n))
	       (do
		(if-let [rs (.getRelationships n)]
		  (doseq [r rs]
		    (.delete r)))
		(.delete n))))

(defn relationships
  "Returns all the relationships attached to this node."
  ([#^Node n #^Direction d] (.getRelationships n d))
  ([#^Node n #^Keyword type #^Direction d]
     (.getRelationships n (relationship type) d))
  ([n]
     (cond-mapped n
		  (relationships (:node n))
		  (.getRelationships n))))

(defn single-relationship
  "Returns the only relationship for the node of the given type and
  direction."
  [n type dir]
  (cond-mapped n
	       (single-relationship (:node n) type dir)
	       (.getSingleRelationship n (relationship type) dir)))

(defn traverse
  "Traverse the graph.  Starting at the given node, traverse the graph
  in either bread-first or depth-first order, stopping when the
  stop-fn returns true.  The filter-fn should return true for any node
  reached during the traversal that is to be returned in the sequence.
  The map of relationships and directions is used to decide which
  edges to traverse."
  [#^Node start-node order stop-evaluator return-evaluator
   relationship-direction]
  (.getAllNodes (.traverse start-node 
			   order
			   stop-evaluator
			   return-evaluator
			   (into-array
			    Object
			    (mapcat identity (map (fn [[k v]] 
						    [(relationship k) v]) 
						  relationship-direction))))))

(defn lookup
  "Inside a transation, looks up nodes by index key for the given index."
  [idx & ids]
  (doall (map (fn [k] (.getSingleNodeFor idx k)) 
              ids)))


(defn new-props-evaluator
  "Creates a ReturnableEvaluator for use with a traverser that returns
  nodes that match the specified property values. propmap is a map that
  defines key value pairs that should ReturnableEvaluator should match
  on."
  [propmap]
  
  (return-if
   (fn [currentPos]
     (let [node (.currentNode currentPos)
	   nodeprops (properties node)
	   ks (keys propmap)
	   nks (count ks)]
       (cond
	(== nks 0) false
	(== nks 1) (and (contains? nodeprops (first ks))
			(= (get nodeprops (first ks))
			   (get propmap (first ks))))
	:else 
	(let [propcomp (fn [key] (= (get nodeprops key) (get propmap key)))]
	  (and (reduce #(and (contains? nodeprops %1)
			     (contains? nodeprops %2))
		       ks)
	       (reduce #(and (propcomp %1) (propcomp %2)) ks))))))))


(defn find-by-props
  "Finds a node by traversing outward from start-node along
  relationship relkey.  The function looks for nodes with property
  values matching the key value pairs in propmap."
  [start-node relkey propmap]
  (traverse start-node
	      breadth-first
	      end-of-graph
	      (new-props-evaluator propmap)
	      {relkey outgoing}))
