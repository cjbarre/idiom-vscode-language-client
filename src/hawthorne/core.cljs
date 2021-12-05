(ns hawthorne.core
  (:require ["vscode" :as vscode :refer [window]]))

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
  (register-command! "hawthorne.sayHello" say-hello)
  (prn "Hawthorne activated"))

(defn deactivate
  []
  (dispose-all! @disposables)
  (prn "Hawthorne deactivated"))

(defn before-load-async [done]
  (deactivate)
  (done))

(defn after-load []
  (activate @current-context)
  (prn "Hawthorne reloaded"))
  