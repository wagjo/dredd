(ns dredd.data.questions
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
                "a zakladne prikazy Karola a jazyka C.")}
   {:id "q9"
    :name "Vypis na obrazovku - C 1"
    :text #(str "Napiste funkciu, ktora ma jeden vstupny parameter x, typu "
                (rand-nth ["float" "double"])
                ". Tato funkcia nech vypise na obrazovku text \"Vysledok je \" a nech vypise "
                (rand-nth ["dvojnasobok" "trojnasobok" "polovicu"])
                " hodnoty v premennej x s presnostou na 3 desatinne miesta.")}
   {:id "q10"
    :name "Nacitanie z klavesnice - C 2"
    :text #(str "Napiste funkciu, vracajucu realne cislo, ktora nacita 3 realne cisla z klavesnice a vrati ich "
                (rand-nth ["priemer" "maximalnu hodnotu" "minimalnu hodnotu"])
                ".")}
   {:id "q11"
    :name "Cyklus for"
    :text #(str "Napiste funkciu, ktora vypise na obrazovku vsetky "
                (rand-nth ["nasobky cisla " "mocniny cisla "])
                (rand-nth ["3" "5" "7" "11"])
                " mensie ako "
                (rand-nth ["3000" "5000" "7000" "11000"])                
                ".Pouzite cyklus for a zakladne prikazy jazyka C.")}
   {:id "q12"
    :name "Viac vstupnych parametrov"
    :text #(str "Napiste funkciu, ktora ma tri vstupne parametre, znak, cele cislo a realne cislo. Vasa funkcia nech vsetky tieto hodnoty vypise na obrazovku, kazdu na samostany riadok.")}
   {:id "q13"
    :name "Praca so znakmi 1"
    :text #(str "Napiste funkciu, vracajucu znak a ktora ma 1 vstupny parameter tiez typu znak. Vasa funkcia nech vrati "
                (rand-nth ["vstupny znak. Ak vsak vstupny znak bola samohlaska, vratte ju prevedenu na velke pismeno."
                           "vstupny znak. Ak vsak vstupny znak bol ypsilon, vratte makke i, a naopak, ak bol vstupny znak makke i, vratte ypsilon."
                           "vstupny znak. Ak vsak vstupny znak bola samohlaska, vratte ju prevedenu na male pismeno."
                           "vstupny znak. Ak vsak vstupny znak bola medzera, ciarka, alebo bodka, vratte znak pomlcka ('-')"]))}
   {:id "q14"
    :name "Nahodne cisla"
    :text #(str "Napiste funkciu, vracajucu cele cislo, ktora vypise na obrazovku "
                (rand-nth ["15" "20" "25" "30"])
                " nahodnych celych cislel, a "
                (rand-nth ["piate" "desiate" "siedme" "posledne" "prve"])
                " z tych nahodnych cisel nech funkcia aj vrati."
                " Pomocka: Nahodne cisla sa generuju pomocou funkcie rand() ktora vracia nahodne cele cislo. Na zaciatku pred ich generovanim treba vykonat prikaz srand(time(NULL)), aby sa generator nahodnych cisel nainicializoval. ")}
   ;; vratit i-ty znak z retazca
   ;; vratit 5. znak z retazca
   ;; vratit dlzku retazca
   ;; tie iste, ale vypisat na obrazovku, resp. nacitat z klavesnice vstup 
   ;; zistit ci je znak pismeno alebo cislo alebo co to je
   {:id "q15"
    :name "Praca s polom 1"
    :text #(str "Napiste funkciu, ktora ma vstupny parameter typu retazec (smernik na znak). V tomto retazci nech vasa funkcia"
                (rand-nth ["zameni vsetky samohlasky (bez diakritiky) na velke pismena"
                           "zameni vsetky pismena okrem samohlasok (bez diakritiky) na velke pismena"
                           "zameni kazde tretie pismeno za velke"
                           "nahradi medzery, ciarky a bodky pomlckami"])
                ". Nakoniec nech vasa funkcia vysledny retazec vypise na obrazovku. Pomocka: K retazcu sa mozete spravat ako ku polu a retazec modifikujte v cykle znak po znaku.")}
   {:id "q16"
    :name "Praca s polom 2"
    :text #(str "Napiste funkciu, ktora ma vstupny parameter typu retazec (smernik na znak). Vasa funkcia nech "
                (rand-nth ["vypise na obrazovku tento retazec odzadu"
                           "vypise na obrazovku retazec bez samohlasok (retazec je bez diakritiky)"
                           "vypise na obrazovku kazde druhy znak z retazca"
                           "vypise na obrazovku retazec bez medzier, ciarok a bodiek"])
                ". Pomocka: K retazcu sa mozete spravat ako ku polu a vypisujte postupne v cykle znak po znaku.")}
   {:id "q17"
    :name "Praca s Retazcom 1"
    :text #(str "Napiste funkciu, vracajucu cislo, ktora ma vstupny parameter typu retazec (smernik na znak). Vasa funkcia nech vrati cislo reprezentujuce dlzku vstupneho textu.")}
   {:id "q18"
    :name "Praca s Retazcom 2"
    :text #(str "Napiste funkciu, ktora ma vstupny parameter typu retazec (smernik na znak). Vasa funkcia nech vypise na obrazovku "
                (rand-nth ["5." "6." "10." "1."])
                " znak vstupneho textu.")}
   {:id "q21zal"
    :name "Praca s Retazcom 1"
    :text #(str "Napiste funkciu, ktora ma vstupny parameter typu retazec (smernik na znak). Vasa funkcia nech vypise na obrazovku zadany retazec a na samostatny riadok aj jeho dlzku.")}
   {:id "q22zal"
    :name "Praca s Retazcom 2"
    :text #(str "Napiste funkciu, vracajucu znak, ktora ma vstupny parameter typu retazec (smernik na znak). Vasa funkcia nech vrati "
                (rand-nth ["5." "6." "10." "1."])
                " znak vstupneho textu.")}
   {:id "q21"
    :name "Praca so znakmi"
    :text #(str "Napiste funkciu, vracajucu cele cislo, ktora ma vstupny parameter typu znak. Vasa funkcia nech zisti, ci zadany vstupny znak je alebo nie je "
                (rand-nth ["cislica. Ak bude vstupny znak cislica"
                           "samohlaska. Ak bude vstupny znak samohlaska"])
                ", vasa funkcia nech vrati cislo 1, ak nie, tak nech vrati 0.")}
   {:id "q22"
    :name "Praca s cislami"
    :text #(str "Napiste funkciu, ktora ma 2 vstupne parametre typu cele cislo. Vasa funkcia nech vypise na obrazovku dane cisla a potom zvysok po deleni tychto dvoch cisel, vsetko na samostatny riadok.")}
   {:id "q23-1"
    :name "Definicia struktury"
    :text #(str "Zadefinujte strukturu s nazvom Osoba, ktora bude obsahovat 3 prvky: meno typu retazec, vek typu cele cislo a pohlavie typu znak. Vytvorte tiez 2 premenne 'adam' a 'eva', ktore budu typu tejto vasej struktury.")}
   {:id "q23"
    :name "Definicia enumu"
    :text #(str "Zadefinujte enumeracny typ s nazvom Den, ktory bude nadobudat 7 hodnot: PO, UT, ST, STV, PI, SO a NE. Ciselne reprezentacie tychto hodnot nastavte tak, aby NE bola 0, PO 1, UT 2, ST 3, STV 4, PI 5 a SO bola 6. Vytvorte tiez 2 premenne 'den_a' a 'den_b', ktore budu tohto vasho enumeracneho typu.")}
   {:id "q23-2"
    :name "Definicia struktury"
    :text #(str "Zadefinujte strukturu s nazvom Adresa, ktora bude obsahovat 4 prvky: ulica typu retazec, cislo typu cele cislo, mesto typu retazec a stat typu znak. Vytvorte tiez 2 premenne 'trvale_bydlisko' a 'dodacia_adreda', ktore budu typu tejto vasej struktury.")}
   {:id "q24"
    :name "Nahodne cisla"
    :text #(str "Napiste funkciu, vracajucu cele cislo, ktora vypise na obrazovku "
                (rand-nth ["15" "20" "25" "30"])
                " nahodnych celych cislel, a "
                (rand-nth ["piate" "desiate" "siedme" "posledne" "prve"])
                " z tych nahodnych cisel nech funkcia aj vrati."
                " Pomocka: Nahodne cisla sa generuju pomocou funkcie rand() ktora vracia nahodne cele cislo. Na zaciatku pred ich generovanim treba vykonat prikaz srand(time(NULL)), aby sa generator nahodnych cisel nainicializoval. ")}
   {:id "q25"
    :name "Euler 0.5"
    :text #(str "Napiste funkciu, vracajucu cele cislo, ktora vrati sucet vsetkych nasobkov cisla 3 mensich ako 1000.")}
   {:id "q26-1"
    :name "Subor 1"
    :text #(str "Napiste funkciu, ktora ma 3 vstupne parametre typu cele cislo. Vasa funkcia nech tieto cisla zapise do suboru s nazvom 'vystup.txt'.")}
   {:id "q27-1"
    :name "Subor 2"
    :text #(str "Napiste funkciu, ktora vrati cislo reprezentujuce pocet riadkov (riadky su samozrejme oddelene znakom '\n') v subore s nazvom 'vstup.txt'")} 
   {:id "q26"
    :name "Subor 1"
    :text #(str "Napiste funkciu, ktora ma 2 vstupne parametre typu realne cislo. Vasa funkcia nech tieto cisla zapise do suboru s nazvom 'vystup.txt', kazdu na samostatny riadok.")}
   {:id "q27"
    :name "Subor 2"
    :text #(str "Napiste funkciu, ktora vrati cislo reprezentujuce pocet bodiek a ciarok v subore s nazvom 'vstup.txt'")} 
   ])
;; napisat funkciu ktora cisti ci je cislo parne, pomocou pouzitia
;; zvysku po deleni
;; funkcia, ktora nacitava cisla az kym neni 0


(defn get-question [id]
  (find-first #(= id (:id %)) questions))

(defn instantiate-question [id]
  (let [q (get-question id)]
    (assoc q :text ((:text q)))))

;; Examples

(comment

  (instantiate-question "q3")

  )
