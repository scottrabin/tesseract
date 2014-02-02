(ns tesseract.impl
  (:require
    [clojure.string]))

(defprotocol IRender
  (render [item]
          "Render an item into a string"))

(extend-protocol IRender
  String
  (render [string]
    string))

(defprotocol IAttributeValue
  (to-attr [this] "Generate an appropriate attribute value string"))

(def HTML_ATTR_ESCAPE {\< "&lt;"
                       \> "&gt;"
                       \" "&quot;"
                       \' "&apos;"
                       \& "&amp;"})
(extend-protocol IAttributeValue
  String
  (to-attr [this]
    (clojure.string/escape this HTML_ATTR_ESCAPE))

  clojure.lang.Keyword
  (to-attr [this]
    (to-attr (name this)))

  clojure.lang.LazySeq
  (to-attr [this]
    (clojure.string/join " " (map to-attr this)))

  clojure.lang.PersistentList
  (to-attr [this]
    (to-attr (map to-attr this)))

  clojure.lang.PersistentHashSet
  (to-attr [this]
    (to-attr (map to-attr this)))

  clojure.lang.PersistentArrayMap
  (to-attr [this]
    (to-attr (for [[k v] this :when v] k)))

  clojure.lang.PersistentTreeMap
  (to-attr [this]
    (to-attr (for [[k v] this :when v] k)))

  clojure.lang.PersistentTreeSet
  (to-attr [this]
    (to-attr (map to-attr this))))
