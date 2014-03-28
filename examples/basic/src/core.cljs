(ns examples.basic.core
  (:require [tesseract.core :as tesseract :include-macros true]
            [tesseract.dom :as dom]))

(tesseract/defcomponent ExampleApplication
  (render [{attrs :attrs :as component}]
          (let [dsec (Math/round (/ (:elapsed attrs) 100))
                seconds (str (/ dsec 10)
                             (when (zero? (mod dsec 10)) ".0"))
                message (str "Tesseract has successfully been running for "
                             seconds " seconds.")]
            (dom/div {} message))))

(set!
  (.-onload js/window)
  (fn []
    (let [start (.getTime (js/Date.))
          container (.getElementById js/document "container")
          tick #(tesseract/mount-into-container!
                  (ExampleApplication {:elapsed (- (.getTime (js/Date.)) start)})
                  container)]

      (.setInterval js/window tick 50))))

