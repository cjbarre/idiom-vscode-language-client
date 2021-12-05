(ns hawthorne.core
  (:require ["vscode" :as vscode :refer [window]]))

(defn say-hello []
  (.. window (showInformationMessage "Hello world!!")))

(defn activate
  [^js context]
  (js/console.log "Hawthorne activating")
  (.. vscode -commands (registerCommand "hawthorne.sayHello" say-hello)))

(defn deactivate
  []
  (js/console.log "Hawthorne deactivating"))