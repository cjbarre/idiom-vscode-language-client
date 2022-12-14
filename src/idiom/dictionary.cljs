(ns idiom.dictionary
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require ["fs" :as fs]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [clojure.reader :refer [read-string]]
            ["vscode" :as vscode :refer [window languages]]
            [cljs-http.client :as httpc]
            [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [<! take!]]))

#_(set! js/XMLHttpRequest (nodejs/require "xhr2"))

;; fs.readFile('/Users/joe/test.txt', 'utf8', (err, data) => {
;;   if (err) {
;;    console.error(err);
;;    return;
;;  }
;;  console.log(data);
;; });
fs 
(defonce dictionary (atom nil))

(defn load-dictionary
  [path]
  (fs/readFile path "utf8" 
            (fn [err data]
              (if err 
                (prn err)
                (reset! dictionary (read-string data))))))

#_(load-dictionary "/Users/cam/Code/tibet/idiom/dictionary.edn")

(defn handle-special-cases
  [word]
  (-> word
      (clojure.string/replace #"pa'i" "pa")
      (clojure.string/replace #"ba'i" "ba")))

(defn lookup-word [dictionary text]
  (loop [text (->> (string/lower-case text)
                   (handle-special-cases))
         drop-num 0]
    (let [term-seq (->> (-> text
                            (string/split #"\s"))
                        (drop-last drop-num))
          proposed-term (if (= 1 (count term-seq))
                          (-> (first term-seq)
                              #_(string/replace #"ts" "tsh")
                              #_(string/replace #"tz" "ts"))
                          (-> (->> term-seq
                                   (string/join " ")
                                   (string/lower-case))
                              #_(string/replace #"ts" "tsh")
                              #_(string/replace #"tz" "ts")))]
      #_(prn term-seq)
      #_(prn proposed-term)
      (if (string/blank? proposed-term)
        nil
        (let [result (get dictionary proposed-term)]
          #_(prn result)
          (cond
            (and (not result) (<= (count term-seq) 1)) nil
            (not result) (recur
                          text
                          (inc drop-num))
            :else proposed-term))))))

(defn lookup-words 
  [dictionary text]
  #_(prn text)
  (loop [current-text text
         acc []]
    (let [word (lookup-word dictionary current-text)
          drop-num (count (string/split word #"\s"))]
      (if (string/blank? current-text)
        acc
        (recur (string/join " " (drop drop-num (string/split current-text #"\s"))) (conj acc word))))))

(defn lookup-sections
  [dictionary text]
  (let [sections (->> (-> text
                          (string/replace #"\n|\r\n" " ")
                          (string/replace #"\^|\#" "")
                          (string/split  #"\,"))
                      (filter #(not (string/blank? %))))]
    (map #(lookup-words dictionary %) sections)))

(defn lookup-definitions
  [dictionary words]
  (map #(assoc {}
               (string/upper-case (or % "Not Found"))
               (sort-by :priority (get-in dictionary [(handle-special-cases %) :definitions])))
       (filter #(not (nil? %)) words)))
 
#_(pprint (lookup-definitions @dictionary [nil "bum" "pa'i" "sgra don" "chos can"]))



#_(take! (lookup-definition dictionary "rang mtshang"))

#_(http/post
 "https://dictionary.christian-steinert.de/dict.php"
 {:form-params {:search "pa"
                :lang "tib"
                :offset "0"}})

#_(def ex "DUS RUNG GI SMAN DANG DUS RUNG MA YIN PA'I SMAN 'DRES MAR DUS MA YIN PAR SPYOD DGOS TSE KHYIM PA'I MIG SNGAR SPYOD PA DANG")

#_(lookup-sections @dictionary ex)

#_(lookup-word @dictionary "spyod dgos")

#_(get @dictionary (->> (-> ex
                          (string/split #"\s"))
                      (drop-last 6)
                      (string/join " ")
                      (string/lower-case)))

#_(get @dictionary "dang dus")

#_(clojure.pprint/pprint (lookup-definitions @dictionary ["rig pa'i"]))

#_(lookup-sections @dictionary "RIG PA'I SGRON MA 'BAR BA NGAS BSTAN NAS")