(ns tesseract.cursor
  #+cljs (:require cljs.reader))

(def cursor vector)

(defn assoc-cursor
  [component cursor]
  (vary-meta component assoc ::cursor cursor))

(defn get-cursor
  [component]
  (::cursor (meta component)))
