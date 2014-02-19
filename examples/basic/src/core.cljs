(ns examples.basic.core
  (:require [tesseract.core :as tesseract :include-macros true]
            [tesseract.dom :as dom]))

(tesseract/defcomponent ExampleApplication
  (render [{attrs :attrs :as component}]
          (.log js/console "Rendering...")
          (dom/div {} (str "Elapsed: " (:elapsed attrs)))))

(set!
  (.-onload js/window)
  (fn []
    (let [start (.getTime (js/Date.))
          container (.getElementById js/document "container")
          tick #(tesseract/mount-into-container!
                  (ExampleApplication {:elapsed (- (.getTime (js/Date.)) start)})
                  container)]
      (.setInterval js/window tick 500))))
