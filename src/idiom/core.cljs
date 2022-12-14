(ns idiom.core
  (:require ["vscode" :as vscode :refer 
             [window 
              languages 
              Hover editor 
              Range 
              workspace 
              Uri 
              Position]]
            [idiom.dictionary :refer 
             [lookup-words 
              dictionary 
              load-dictionary 
              lookup-sections 
              lookup-definitions]]
            [promesa.core :as p]
            [clojure.string :as string]
            [clojure.pprint :refer 
             [pprint]]
            ["fs" :as fs]
            ["tibetan-ewts-converter" :as ewts :refer [EwtsConverter]]))

(defonce converter (EwtsConverter.))

(comment
  

  (.to_unicode converter "cigs")
  )

(defn format-definitions
  [dictionary-results]
  (map
   (fn [x]
     (str
      (-> x first key)
      " (" (.to_unicode converter (clojure.string/lower-case (-> x first key))) ")" 
      "\n\n"
      (string/join "\n\n"
                   (map
                    (fn [{:keys [source definition]}]
                      (str source " \n" "- " definition))
                    (-> x first val)))))
   dictionary-results))

(defn eval-selection []
  (let [editor (.-activeTextEditor window)
        selection-range (Range. (.. editor -selection -start)
                                (.. editor -selection -end))
        selected-text (.getText (.. editor -document) selection-range)
        text-sections (lookup-sections @dictionary selected-text)
        section-definitions (map #(string/join "\n\n"
                                               (format-definitions (lookup-definitions @dictionary %)))
                                 text-sections)
        uri (.parse Uri (-> (.. workspace -workspaceFolders) first js->clj (get "uri") (str "/output.idiom-repl")))
        display-text (str selected-text
                          "\n\n"
                          (string/join "\n\n----\n\n" section-definitions))]
    (-> (.openTextDocument workspace uri)
        (p/then #(.showTextDocument window % 1 true))
        (p/then #(.edit % (fn [edit] (.insert edit (Position. 0 0) (str display-text "\n\n" "-----" "\n" "-----" "\n\n"))))))))

(defonce current-context (atom nil))

(defonce disposables (atom []))

(defn add-disposable! [disposable]
  (swap! disposables conj disposable))

(defn dispose-all! 
  [disposables]
  (run! (fn [^js disposable]
          (.. disposable (dispose)))
        disposables))



(defn register-command!
  [command-name command-function]
  (let [disposable (.. vscode -commands (registerCommand
                                         command-name
                                         command-function))]
    (add-disposable! disposable)))

(defn activate
  [^js context]
  (reset! current-context context)
  (load-dictionary "/Users/cam/Code/tibet/idiom/dictionary.edn")
  (register-command! "idiom.evalSelection" eval-selection)
  (add-disposable! (.. vscode -languages
                       (registerHoverProvider
                        "idiom-fmt"
                        #js {:provideHover (fn [document _ _]
                                             (let [editor (.-activeTextEditor window)
                                                   selection-range (Range. (.. editor -selection -start)
                                                                           (.. editor -selection -end))
                                                   selected-text (.getText document selection-range)
                                                   text-sections (lookup-sections @dictionary selected-text)
                                                   section-definitions (map #(string/join "\n\n"
                                                                                          (format-definitions (lookup-definitions @dictionary %)))
                                                                            text-sections)
                                                   uri (.parse Uri (-> (.. workspace -workspaceFolders) first js->clj (get "uri") (str "/output.idiom-repl")))
                                                   display-text (str selected-text
                                                                     "\n\n"
                                                                     (string/join "\n\n----\n\n" section-definitions))]
                                               
                                               (Hover. (str selected-text
                                                            "\n\n"
                                                            (string/join "\n\n----\n\n" section-definitions)))))})))

  (prn "Idiom activated"))

(defn deactivate
  []
  (dispose-all! @disposables)
  (prn "Idiom deactivated"))

(defn before-load-async [done]
  (deactivate)
  (done))

(defn after-load []
  (activate @current-context)
  (prn "Idiom reloaded"))

#_(.getText d (.getWordRangeAtPosition d (.. e -selection -active)))

#_(.getText d (.getWordRangeAtPosition d (.. e -selection -end)))

#_(.compareTo (.. e -selection -end) (.. e -selection -start))

