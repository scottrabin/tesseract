(ns tesseract.element
  (:require [clojure.string]))

(defn process-props
  "Process incoming element properties"
  [props]
  {:attrs props})

(defmacro defelement
  [tag]
  `(def ~(symbol (str *ns*) (str tag))
     (fn [props# & children#]
       (merge {:tag ~(keyword tag)
               :attrs {}
               :children (or children# [])}
              (process-props props#)))))

(defn to-attr
  "Translate an attribute key value pair to a string"
  ([pair]
   (to-attr (first pair) (second pair)))
  ([k v]
   (str (name k) "=\"" v "\"")))

(defn render
  "Renders an element data structure into a valid string"
  [element]
  (let [tag (name (:tag element))
        attr-string (when-not (empty? (:attrs element))
                      (clojure.string/join " "
                                           (map to-attr (:attrs element))))
        children-string (clojure.string/join
                          (map render (:children element)))
        open-tag (if (nil? attr-string)
                   (str "<" tag ">")
                   (str "<" tag " " attr-string ">"))
        close-tag (str "</" tag ">")]
    (str open-tag children-string close-tag)))
