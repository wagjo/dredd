;; Copyright (C) 2011, Jozef Wagner. All rights reserved.
;; This ns is forked from hgavin/clojure-neo4j

(ns dredd.db-adapter.neo4j
  "Neo4j wrapper, forked from hgavin/clojure-neo4j"
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

;; Wrapper for Neo4j. Because I was not happy with the current state
;; (01/2011) of existing Neo4j wrappers for clojure, I've decided to
;; create my own.
;;
;; Purpose of this ns is to provide intiutive access to commonly used
;; Neo4j operations. It uses official Neo4j Java bindings. It does not
;; use Blueprints interface.
;;
;; Disclaimer: I have forked hgavin/clojure-neo4j and modified it
;; heavily.
;;
;; Disclaimer: Some comments and docs are taken from official Neo4j javadocs.
;;
;;; Usage:
;; - use with-db! to establish a connection to the database
;; - all db operations must be inside with-db! body
;;
;;; Code notes:
;; - *neo-db* holds the current db instance, so that users do not have
;;   to supply db instance at each call to db operations. This
;;   approach has of course its drawbacks, but I've found it suitable
;;   for my purposes.

;;;; Implementation details

(defonce ^{:private true
           :doc "Holds the current database instance"
           :tag EmbeddedGraphDatabase}
  *neo-db* nil)

(defn- array? [x]
  "Determine whether x is an array or not"
  (-> x class .isArray))

(defn- name-or-str [x]
  "If x is keyword, returns its name. If not, stringify the value"
  (if (keyword? x) 
    (name x) 
    (str x)))

(defn- relationship [k]
  "Create java class inplementing RelationshipType. Used for interop.
  NOTE: Should we cache these instances to save memory?
  Or will they be GCd?"
  (proxy [RelationshipType] []
    (name [] (name k))))

(defn- start! [path]
  "Establish a connection to the database.
  Uses *neo-db* to hold the connection."
  (io!)
  (let [n (EmbeddedGraphDatabase. path)]
    (alter-var-root #'*neo-db* (fn [_] n))))

(defn- stop! []
  "Closes a connection stored in *neo-db*"
  (io!)
  (.shutdown *neo-db*))

(defn- process-position [^TraversalPosition p]
  (let [a (.currentNode p)]
    (swank.core/break))
  {:node (.currentNode p)
   :depth (.depth p)
   :start? (.isStartNode p)
   :last-rel (.lastRelationshipTraversed p)
   :prev-node (.previousNode p)
   :count (.returnedNodesCount p)})


;;;; Public API

(defmacro with-db! [path & body]
  "Establish a connection to the neo db.
  Because there is an overhead when establishing connection, users should
  not call this macro often. Also note that this macro is not threadsafe."
  (io!)
  `(do
     ;; Not using binding macro, because db should be accessible
     ;; from multiple threads.
     (start! path)
     (try
       ~@body
       (finally (stop!)))))

(defmacro with-tx [& body]
  "Establish a transaction. Use it for mutable db operations.
  If you do not want to commit it, throw an exception."
  `(let [tx# (.beginTx *neo-db*)]
     (try
       (let [val# (do ~@body)]
         (.success tx#)
         val#)
       (finally (.finish tx#)))))

(defn id [item]
  "Returns id for a given node or relationship"
  (.getId item))

(defn delete! [item]
  "Deletes node or relationship.
  Only node which has no relationships attached to it can be deleted."
  (io!)
  (.delete item))

;;; Relationships

;; Relationship directions used when getting relationships from a
;; node or when creating traversers.
;;
;; A relationship has a direction from a node's point of view. If a
;; node is the start node of a relationship it will be an OUTGOING
;; relationship from that node's point of view. If a node is the end
;; node of a relationship it will be an INCOMING relationship from
;; that node's point of view. The BOTH direction is used when
;; direction is of no importance, such as "give me all" or "traverse
;; all" relationships that are either OUTGOING or INCOMING. (taken
;; from Neo4j javadoc)

(def both ^{:doc "Defines both incoming and outgoing relationships."}
     Direction/BOTH)

(def incoming ^{:doc "Defines incoming relationships."}
     Direction/INCOMING)

(def outgoing  ^{:doc "Defines outgoing relationships."}
     Direction/OUTGOING)

(defn rel?
  "Returns true if relationship exists, false otherwise.
  Allowed arguments:
  - [node] - test for any relationship
  - [node direction] - any relationships with specified direction
  - [node direction type] - relationship of specified type and
                            of specified direction
  - [node type & types] - relationship of any of specified types with
                          any direction"
  ([^Node node]
     (.hasRelationship node))
  ([^Node node type-or-direction & types]
     (let [direction-provided? (= Direction (class type-or-direction))
           ^Direction dir (if direction-provided?
                            type-or-direction
                            both)
           t (map relationship
                  (if direction-provided?
                    (do
                      ;; if multiple types, direction is both
                      (when (> (count types) 1)
                        (throw (IllegalArgumentException.
                                "Cannot specify direction if multiple types are requested")))
                      types)
                    (cons type-or-direction types)))]
       (cond
        (empty? t) (.hasRelationship node dir)
        (= 1 (count t)) (.hasRelationship node (first t) dir)
        ;; TODO: Is there a way to type hint array in following call?
        :else (.hasRelationship node (into-array RelationshipType t))))))

(defn rels
  "Returns all the relationships attached to this node.
  Allowed arguments:
  - [node] - All relationships
  - [node direction] - All relationships with specified direction
  - [node direction type] - Relationships of specified type and
                            of specified direction
  - [node type & types] - Relationships of any of specified types with
                          any direction"
  ([^Node node]
     (.getRelationships node))
  ([^Node node type-or-direction & types]
     (let [direction-provided? (= Direction (class type-or-direction))
           ^Direction dir (if direction-provided?
                            type-or-direction
                            both)
           t (map relationship
                  (if direction-provided?
                    (do
                      ;; if multiple types, direction is both
                      (when (> (count types) 1)
                        (throw (IllegalArgumentException.
                                "Cannot specify direction if multiple types are requested")))
                      types)
                    (cons type-or-direction types)))]
       (cond
        (empty? t) (.getRelationships node dir)
        (= 1 (count t)) (.getRelationships node (first t) dir)
        ;; TODO: Is there a way to type hint array in following call?
        :else (.getRelationships node (into-array RelationshipType t))))))

(defn single-rel [^Node node type direction]
  "Returns the only relationship for the node of the given type and
  direction."
  (.getSingleRelationship node (relationship type) direction))

(defn create-rel! [^Node from type ^Node to]
  "Create relationship of a supplied type between from and to nodes."
  (io!)
  (.createRelationshipTo from to (relationship type)))

(defn nodes [^Relationship r]
  "Returns the two nodes attached to the given relationship."
  (.getNodes r))

(defn other [^Relationship r ^Node node]
  "Returns other node for given relationship."  
  (.getOtherNode r node))

(defn rel-type [^Relationship r]
  "Returns type of given relationship."  
  (keyword (.name (.getType r))))

;;; Properties

(defn prop? [^PropertyContainer c k]
  "Returns true if given node or relationship contains
  property with a given key"
  (.hasProperty c (name-or-str k)))

;; TODO: add fns which are less resource consuming :)

;; Props fetch all properties and can be very resource consuming if
;; node contains many properties.
(defn props [^PropertyContainer c]
  "Return map of properties for a given node or relationship."
  (let [keys (.getPropertyKeys c)
        convert-fn (fn [k] [(keyword k)
                           (let [v (.getProperty c k)]
                             (if (array? v) ; handle multiple values
                               (seq v)
                               v))])]
    (into {} (map convert-fn keys))))

(defn set-prop!
  "Sets or remove property for a given node or relationship.
  The property value must be one of the valid property types (see Neo4j docs).
  If a property value is nil, removes this property from the given
  node or relationship."
  ([^PropertyContainer c key]
     (set-prop! c key nil))
  ([^PropertyContainer c key value]
     (io!)
     (if value
       ;; TODO: better support primivives and arrays of primitives
       (.setProperty c (name-or-str key)
                     (if (coll? value) ; handle multiple values
                       (into-array String value)
                       value))
       (.removeProperty c (name-or-str key)))))

(defn set-props! [^PropertyContainer c props]
  "Sets properties for a given node or relationship.
  The property value must be one of the valid property types (see Neo4j docs).
  If a property value is nil, removes this property from the given
  node or relationship."
  (io!)
  (doseq [[k v] props]
    (set-prop! c k v)))

;;; Nodes

(defn root []
  "Returns reference/root node."
  (.getReferenceNode *neo-db*))

(defn create-node!
  "Creates a new node."
  ([]
     (io!)
     (.createNode *neo-db*))
  ([props]
     (doto (create-node!)
       (set-props! props))))

(defn create-child!
  "Creates a node that is a child of the specified parent node
  (or root node) along the specified relationship.
  props is a map that defines the properties of the node."
  ([type props]
     (create-child! (root) type props))
  ([node type props]
     (io!)
     (let [child (create-node! props)]
       (create-rel! node type child)
       child)))

(defn delete-node! [node]
  "Delete node and all its relationships."
  (io!)
  (doseq [r (rels node)]
    (delete! r))
  (delete! node))

;;; Graph traversal helpers

;; Nodes can be traversed either breadth first or depth first. A depth
;; first traversal is often more likely to find one matching node
;; before a breadth first traversal. A breadth first traversal will
;; always find the closest matching nodes first, which means that
;; TraversalPosition.depth() will return the length of the shortest
;; path from the start node to the node at that position, which is not
;; guaranteed for depth first traversals. (taken from Neo4j docs)

;; A breadth first traversal usually needs to store more state about
;; where the traversal should go next than a depth first traversal
;; does. Depth first traversals are thus more memory efficient. (taken
;; from Neo4j docs)

(def breadth-first ^{:doc "Sets a breadth first traversal meaning the traverser will traverse all relationships on the current depth before going deeper."}
     Traverser$Order/BREADTH_FIRST)

(def depth-first ^{:doc "Sets a depth first traversal meaning the traverser will go as deep as possible (increasing depth for each traversal) before traversing next relationship on same depth."}
     Traverser$Order/DEPTH_FIRST)


(defn depth-of [d]
  "Return a StopEvaluator for the given traversal depth."
  (if (== d 1) 
    StopEvaluator/DEPTH_ONE
    (proxy [StopEvaluator] []
      (isStopNode [^TraversalPosition pos]
                  (== (.depth pos) d)))))

(def end-of-graph ^{:doc "Traverse until the end of the graph."}
     StopEvaluator/END_OF_GRAPH)

(defn stop-if [f]
  "Custom StopEvaluator, f should return true when at stop node. f takes one
  argument. The function will be passed the current position map to act on."
  (proxy [StopEvaluator] []
    (isStopNode [^TraversalPosition p] (f (process-position p)))))


(def return-all ^{:doc "A returnable evaluator that returns all nodes encountered."}
     ReturnableEvaluator/ALL)

(def return-all-but-start ^{:doc "A returnable evaluator that returns all nodes except the start node."}
     ReturnableEvaluator/ALL_BUT_START_NODE)

(defn return-if [f]
  "Custom ReturnableEvaluator, f should return true if node at current
  position should be returned. Takes a function of one argument.
  The function will be passed the current position map to act on."
  (proxy [ReturnableEvaluator] []
    (isReturnableNode [^TraversalPosition p] (f (process-position p)))))

(defn return-by-props [props]
  "Creates a ReturnableEvaluator for use with a traverser that returns
  nodes that match the specified property values. propmap is a map that
  defines key value pairs that should ReturnableEvaluator should match
  on."
  (return-if
   (fn [pos]
     (let [node (:node pos)
           check-prop (fn [[k v]] (= v (.getProperty node (name-or-str k) nil)))]
       (every? check-prop props)))))

(defn old-return-by-props [props]
  "Creates a ReturnableEvaluator for use with a traverser that returns
  nodes that match the specified property values. propmap is a map that
  defines key value pairs that should ReturnableEvaluator should match
  on."
  (return-if
   (fn [pos]
     (let [nodeprops (props (:node pos))
	   ks (keys props)
	   nks (count ks)]
       (cond
	(== nks 0) false
	(== nks 1) (and (contains? nodeprops (first ks))
			(= (get nodeprops (first ks))
			   (get props (first ks))))
	:else 
	(let [propcomp (fn [key] (= (get nodeprops key) (get props key)))]
	  (and (reduce #(and (contains? nodeprops %1)
			     (contains? nodeprops %2))
		       ks)
	       (reduce #(and (propcomp %1) (propcomp %2)) ks))))))))

;;; Graph traversal

(defn go [^Node node & types]
  "Walk through the graph by following specified relations. Returns last node.
  Throws NullPointerException if graph cannot be traversed.
  Throws NotFoundException if path is ambiguous."
  (let [next-node (fn [^Node node type]
                    (second (nodes (single-rel node type outgoing))))]
    (reduce next-node node types)))

;; TODO: previest directions na keywordy

(defn traverse [^Node node order stop-eval return-eval rel-dir-map]
  "Traverse the graph. Starting at the given node, traverse the graph
  in either bread-first or depth-first order, stopping based on 
  stop-eval. The return-eval decides which nodes make it into the result.
  The map of relationships and directions is used to decide which
  edges to traverse."  
  (lazy-seq
   (.traverse node order stop-eval return-eval
              (into-array Object
                          (mapcat (fn [[k v]] [(relationship k) v])
                                  rel-dir-map)))))

(defn find-by-props [node rel propmap]
  "Finds a node by traversing outward from start-node along
  relationship relkey. The function looks for nodes with property
  values matching the key value pairs in propmap."
  (traverse node
            depth-first
            end-of-graph
            (return-by-props propmap)
            {rel outgoing}))

;;;; Examples

(comment

  (traverse (root)
            depth-first
            end-of-graph
            return-all
            {:users outgoing})

  (def a (map props (find-by-props (go (root) :users)
                               :user
                               {:uid "jw817dk"})))

  
)
