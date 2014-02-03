(ns tesseract.impl-test
  #+clj (:require [tesseract.impl :as impl]
                  [clojure.test :refer :all])
  #+cljs (:require [tesseract.impl :as impl]
                   [cemerick.cljs.test :as t])
  #+cljs (:require-macros [cemerick.cljs.test :refer [is deftest testing]]))

(defn attr=
  "Compare attribute values to check equality"
  [& attrvals]
  (apply = (map #(set (clojure.string/split % #"\s")) attrvals)))

; TODO
(deftest render)

(deftest to-attr
  (testing "Keywords"
    (is (attr= "value"
               (impl/to-attr :value))))
  (testing "Strings"
    (is (attr= "attribute-value"
               (impl/to-attr "attribute-value")))
    (is (attr= "escaping &lt; &gt; &quot; &apos; &amp;"
               (impl/to-attr "escaping < > \" ' &"))))
  (testing "Lists"
    (is (attr= "first second third"
               (impl/to-attr (list :first "second" "third")))))
  (testing "Vectors"
    (is (attr= "first second third"
               (impl/to-attr ["first" "second" "third"])))
    (is (attr= "keyword second third"
               (impl/to-attr [:keyword "second" "third"])))
    (is (attr= "hash-one hash-two second keyword"
               (impl/to-attr [{:hash-one true
                               :hash-two true}
                              "second"
                              :keyword]))))
  (testing "Hash maps"
    (is (attr= "first second"
               (impl/to-attr {"first" true
                              "second" true})))
    (is (attr= "second only"
               (impl/to-attr {"first" false
                              "second" true
                              "only" true})))
    (is (attr= "vector key other"
               (impl/to-attr {["vector" "key"] true
                              "other" true})))
    (is (attr= "keyword other"
               (impl/to-attr {:keyword true
                              "other" true}))))
  (testing "Sets"
    (is (attr= "first second"
               (impl/to-attr #{:first "second"}))))
  (testing "Sorted map"
    ; attribute value should come out in the sorted order
    (is (= "first second"
           (impl/to-attr (sorted-map-by
                           #(compare (name %1) (name %2))
                           :first true "second" true :third false)))))
  (testing "Sorted set"
    ; attribute value should come out in the sorted order
    (is (= "first second"
           (impl/to-attr (sorted-set-by
                           compare
                           :first :second))))))
