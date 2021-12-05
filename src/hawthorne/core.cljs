(ns hawthorne.core
  (:require ["vscode" :as vscode :refer [window]]))

(defonce current-context (atom nil))
(defonce disposables (atom []))

(defn add-disposable! [disposable]
  (swap! disposables conj disposable))

;; TODO: Complete this function
(defn dispose-disposables! [])

(defn say-hello []
  (.. window (showInformationMessage "Hello world!")))

(defn activate
  [^js context]
  (reset! current-context context)
  (js/console.log "Hawthorne activating")
  ;; TODO: Add this to disposables atom
  (.. context -subscriptions
      (push (.. vscode -commands
                (registerCommand "hawthorne.sayHello" say-hello)))))

;; TODO: Call dispose-disposables!
(defn deactivate
  []
  (js/console.log "Hawthorne deactivating"))

(defn before-load-async [done]
  (prn "running before-load")
  (deactivate)
  (done))

(defn after-load []
  (prn "running after-load")
  (activate @current-context)
  (println "Reloaded"))