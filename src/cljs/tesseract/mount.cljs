(ns tesseract.mount
  "Handles mounting components into and out of env, a map ref"
  (:require [tesseract.dom.core :as dom.core])
  (:import [goog.ui IdGenerator]))

(def ROOT_ID_ATTR "data-tesseractid")
(def DOCUMENT_NODE)

(defn create-root-id []
  (.getNextUniqueId (.getInstance IdGenerator)))

(defn root-element [container]
  (if (= (.-nodeType container) DOCUMENT_NODE)
    (.-documentElement container)
    container))

(defn root-id [container]
  (when-let [root (root-element container)]
    (if-let [id (dom.core/attr root ROOT_ID_ATTR)]
      id
      (let [id (create-root-id)]
        (dom.core/set-attr! root ROOT_ID_ATTR id)
        id))))

(defn component-by-root-id [env id] (get-in @env [:components id]))

(defn container-by-root-id [env id] (get-in @env [:containers id]))

(defn root-ids [env] (keys (:components @env)))
