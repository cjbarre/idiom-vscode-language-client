(ns idiom.core
  (:require ["vscode" :as vscode :refer [window languages Hover editor Range]]
            [idiom.dictionary :refer [lookup-words dictionary load-dictionary lookup-sections]]
            [clojure.string :as string]))

(defonce current-context (atom nil))

(defonce disposables (atom []))

(defn add-disposable! [disposable]
  (swap! disposables conj disposable))

(defn dispose-all! 
  [disposables]
  (run! (fn [^js disposable]
          (.. disposable (dispose)))
        disposables))

(defn say-hello []
  (.. window (showInformationMessage "Hello world!")))

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
  (add-disposable! (.. vscode -languages
                       (registerHoverProvider
                        "idiom-fmt"
                        #js {:provideHover (fn [document _ _]
                                             (let [editor (.-activeTextEditor window)
                                                   selection-range (Range. (.. editor -selection -start)
                                                                           (.. editor -selection -end))
                                                   selected-text (.getText document selection-range)]
                                               (Hover. (str selected-text
                                                            "\n\n"
                                                            (string/join
                                                             "\n\n"
                                                             (lookup-sections @dictionary selected-text))))))})))

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

