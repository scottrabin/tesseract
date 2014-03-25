(ns tesseract.events
  (:require [tesseract.attrs]
            [goog.events]))

(defn root-event-handler [e]
  (.log js/console "Handling:" e))

(doseq [event-name tesseract.attrs/event-names]
  (goog.events/listen (.-body js/document) (name event-name) root-event-handler))
