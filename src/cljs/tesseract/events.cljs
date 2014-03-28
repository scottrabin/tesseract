(ns tesseract.events
  (:require [tesseract.attrs]
            [goog.events]))

(defn root-event-handler [e]
  (.log js/console "Handling:" e))

(defn bind-root-handler! []
  (doseq [event-name tesseract.attrs/event-names]
    (goog.events/listen js/document (name event-name) root-event-handler)))

(defn unbind-root-handler! []
  (doseq [event-name tesseract.attrs/event-names]
    (goog.events/unlisten js/document (name event-name) root-event-handler)))

(bind-root-handler!)
