(ns tesseract.element
  (:require
    [tesseract.impl :as impl]
    [clojure.string]))

(defn to-attr
  "Translate an attribute key value pair to a string"
  ([pair]
   (to-attr (first pair) (second pair)))
  ([k v]
   (str (name k) "=\"" v "\"")))

(defrecord Element [tag attrs children]
  impl/IRender
  (render [_]
    (let [tag-name (-> tag name str)]
      (str
        "<"
        tag-name
        (when-not (empty? attrs)
          (str " " (clojure.string/join " " (map to-attr attrs))))
        ">"
        (when-not (empty? children)
          (clojure.string/join (map impl/render children)))
        "</" tag-name ">"))))

(defmacro defelement
  [tag]
  (let [tag-kw (keyword tag)]
    `(defn ~tag
       [props# & children#]
       (new Element
            ~tag-kw
            props#
            (or children# [])))))
