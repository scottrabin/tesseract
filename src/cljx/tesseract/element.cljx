(ns tesseract.element
  (:require
    [tesseract.impl :as impl]
    [clojure.string]))

(defn to-element-attribute
  "Translate an attribute key value pair to a string"
  ([[k v]]
   (to-element-attribute k v))
  ([k v]
   (str (name k) "=\"" v "\"")))

(defrecord Element [tag attrs children]
  Object
  (toString [_]
    (let [tag-name (-> tag name str)]
      (str
        "<"
        tag-name
        (when-not (empty? attrs)
          (str " " (clojure.string/join " " (map to-element-attribute attrs))))
        ">"
        (when-not (empty? children)
          (clojure.string/join (map str children)))
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
