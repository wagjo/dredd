(ns dredd.questions
  "List of questions"
  (:use [clojure.contrib.seq-utils :only [find-first]]))

(def questions
  [{:id "q1"
    :name "Zakladne pohyby Karola 1"
    :text #(str "Napiste akym sposobom by ste otocili karola "
                (rand-nth ["dolava"
                           "doprava"
                           "o 180 stupnov"
                           "1x dookola (360 stupnov)"
                           "2x dookola (720 stupnov)"]))}
   {:id "q2"
    :name "Zakladne pohyby Karola 2"
    :text #(str "Napiste akym sposobom by ste posunuli karola "
                (rand-nth ["na policko vpravo od karola"
                           "na policko ktore je za karolom"
                           "na policko vlavo od karola"]))}
   {:id "q3"
    :name "Vlastne funkcie - Karol 1"
    :text #(str "Napiste funkciu, ktora "
                (rand-nth ["otoci Karola dozadu"
                           "otoci Karola doprava"
                           "otoci Karola okolo vlastnej osi 1x (360 stupnov)"]))}
   {:id "q4"
    :name "Vlastne funkcie - Karol 2"
    :text #(str "Napiste funkciu, ktora "
                (rand-nth ["pohne Karola 5x dopredu"
                           "pohne Karola 2x dozadu"
                           "pohne Karola o 2 miesta doprava"
                           "pohne Karola o 3 miesta dolava"])
                " pricom Karol sa po skonceni funkcie musi pozerat tym istym smerom, ako na zaciatku")}])

(defn get-question [id]
  (find-first #(= id (:id %)) questions))

(defn instantiate-question [id]
  (let [q (get-question id)]
    (assoc q :text ((:text q)))))

;; Examples

(comment

  (instantiate-question "q3")

  )
