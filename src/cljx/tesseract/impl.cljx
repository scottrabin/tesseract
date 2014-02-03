(ns tesseract.impl
  (:require
    [clojure.string]))

(defprotocol IRender
  (render [item]
          "Render an item into a string"))

(extend-protocol IRender
  #+clj String #+cljs string
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
  #+clj String #+cljs string
  (to-attr [this]
    (clojure.string/escape this HTML_ATTR_ESCAPE))

  #+clj clojure.lang.Keyword #+cljs cljs.core/Keyword
  (to-attr [this]
    (to-attr (name this)))

  #+clj clojure.lang.LazySeq #+cljs cljs.core/LazySeq
  (to-attr [this]
    (clojure.string/join " " (map to-attr this)))

  #+clj clojure.lang.PersistentList #+cljs cljs.core/List
  (to-attr [this]
    (to-attr (map to-attr this)))

  #+clj clojure.lang.PersistentVector #+cljs cljs.core/PersistentVector
  (to-attr [this]
    (to-attr (map to-attr this)))

  #+clj clojure.lang.PersistentHashSet #+cljs cljs.core/PersistentHashSet
  (to-attr [this]
    (to-attr (map to-attr this)))

  #+clj clojure.lang.PersistentArrayMap #+cljs cljs.core/PersistentArrayMap
  (to-attr [this]
    (to-attr (for [[k v] this :when v] k)))

  #+clj clojure.lang.PersistentTreeMap #+cljs cljs.core/PersistentTreeMap
  (to-attr [this]
    (to-attr (for [[k v] this :when v] k)))

  #+clj clojure.lang.PersistentTreeSet #+cljs cljs.core/PersistentTreeSet
  (to-attr [this]
    (to-attr (map to-attr this))))
