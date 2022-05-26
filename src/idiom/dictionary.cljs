(ns idiom.dictionary
  (:require ["fs" :as fs]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [clojure.reader :refer [read-string]]
            ["vscode" :as vscode :refer [window languages]]))

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

(defn lookup-word [dictionary text]
  (loop [text (string/lower-case text)
         drop-num 0]
    (let [term-seq (->> (-> text
                            (string/split #"\s"))
                        (drop-last drop-num))
          proposed-term (if (= 1 (count term-seq))
                          (-> (first term-seq)
                              (string/replace #"ts" "tsh")
                              (string/replace #"tz" "ts"))
                          (-> (->> term-seq
                                   (string/join " ")
                                   (string/lower-case))
                              (string/replace #"ts" "tsh")
                              (string/replace #"tz" "ts")))]
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

(def ex "MKHAR SIL GYI MTSON DON BKA' GDAMS GLEGS BAM LAS ZUR DU BKOL
BA BZHUGS SO, ,NGA")

#_(lookup-sections @dictionary ex)

#_(lookup-word @dictionary "PA")

#_(get @dictionary (->> (-> ex
                          (string/split #"\s"))
                      (drop-last 6)
                      (string/join " ")
                      (string/lower-case)))

