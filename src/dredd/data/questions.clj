;; Copyright (C) 2011, Jozef Wagner. All rights reserved. 

(ns dredd.data.questions
  "List of questions."
  (:refer-clojure :exclude [get])
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
                           "otoci Karola okolo vlastnej osi 1x (360 stupnov)"]) 
                " iba pomocou zakladnych prikazov karola")}
   {:id "q4"
    :name "Vlastne funkcie - Karol 2"
    :text #(str "Napiste funkciu, ktora "
                (rand-nth ["pohne Karola 5x dopredu"
                           "pohne Karola 2x dozadu"
                           "pohne Karola o 2 miesta doprava"
                           "pohne Karola o 3 miesta dolava"])
                " iba pomocou zakladnych prikazov karola, pricom Karol sa po skonceni funkcie musi pozerat tym istym smerom, ako na zaciatku")}
   {:id "q5"
    :name "Funkcie s parametrom - Karol 3"
    :text #(str "Napiste funkciu, ktora ma jeden vstupny parameter x, typu int. Tato funkcia nech "
                (rand-nth ["pohne Karola x krokov dozadu"
                           "pohne Karola x krokov do prava (relativne od smeru akym sa karol pozera)"
                           "pohne Karola x krokov do lava (relativne od smeru akym sa karol pozera)"])
                ". Pouzite iba zakladne prikazy Karola a jazyka C, "
                "pricom Karol sa po skonceni funkcie musi pozerat tym istym smerom, ako na zaciatku")}
   {:id "q6"
    :name "Funkcie s parametrom - Karol 4"
    :text #(str "Napiste funkciu, ktora "
                (rand-nth ["polozi vsetky beepre z Karolovho batohu na zem do miestnosti"
                           "zoberie vsetky beepre z miestnosti do Karolovho batoha"
                           "bude hybat s Karolom dopredu, pokial bude mat Karol po pravej 'ruke' stenu"
                           "bude hybat s Karolom dopredu, pokial bude mat Karol po lavej 'ruke' stenu"
                           "bude hybat s Karolom dopredu, az kym nenajde na zemi beeper"])
                ". Pouzite iba zakladne prikazy Karola a jazyka C.")}
   {:id "q7"
    :name "Funkcie vracajuce hodnotu - Karol 5"
    :text #(str "Napiste funkciu, vracajucu cele cislo, ktora "
                (rand-nth ["polozi vsetky beepre z Karolovho batohu na zem do miestnosti a vrati ich pocet"
                           "bude hybat s Karolom dopredu, az kym nepride ku stene, a vrati pocet prejdenych krokov"])
                ". Pouzite iba zakladne prikazy Karola a jazyka C.")}
   {:id "q8"
    :name "Funkcie s podmienkou - Karol 6"
    :text #(str "Napiste funkciu, ktora ma jeden vstupny parameter x, typu int. Tato funkcia nech "
                (rand-nth ["otoci karola doprava, ak x je vacsie ako 0, a otoci karola dolava, ak x je mensie ako 0. Potom nech sa karol posunie o 1 krok. Mate k dispozicii funkciu turnRight, ktora otoci karolom doprava "
                           "pohne s karolom x krokov dopredu, maximalne vsak 5 krokov. To znamena, ze ak x bude vacsie ako 5, karol sa pohne iba 5 krokov. Mate k dispozicii funkciu movekx, ktora pohne karolom o x krokov dopredu "])
                "a zakladne prikazy Karola a jazyka C.")}])

(defn get [id]
  (find-first #(= id (:id %)) questions))

(defn instantiate [id]
  (let [q (get id)]
    (assoc q :text ((:text q)))))

;; Examples

(comment

  (instantiate-question "q3")

  )
