(ns tesseract.cursor
  #+cljs (:require cljs.reader))

(def cursor vector)

(defn assoc-cursor
  [component cursor]
  (vary-meta component assoc ::cursor cursor))

(defn get-cursor
  [component]
  (::cursor (meta component)))

(defn to-str [cursor]
  (clojure.string/join "-" cursor))

(defn from-str [s]
  (->> (clojure.string/split s #"-")
       (map #+cljs cljs.reader/read-string #+clj read-string) ;; TODO optimize
       (apply cursor)))
