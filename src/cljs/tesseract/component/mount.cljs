(ns tesseract.component.mount
  (:require [tesseract.component.cursor :refer [assoc-cursor]]))

(def ^:private generate-unique-id
  (let [counter (atom 0)]
    (fn [] (swap! counter inc))))

(defn mount!
  [env component container]
  (let [component-id (generate-unique-id)
        cmpt-with-cursor (assoc-cursor component [component-id])]
    (swap! env assoc-in [:components component-id] cmpt-with-cursor)
    (set! (.-innerHTML container) (str (render cmpt-with-cursor)))))
