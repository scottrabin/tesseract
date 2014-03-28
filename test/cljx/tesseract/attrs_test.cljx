(ns tesseract.attrs-test
  #+clj (:require [clojure.test :refer :all]
                  [tesseract.attrs :as attrs]
                  [tesseract.dom :as dom]
                  [tesseract.env :as env]
                  [tesseract.cursor])
  #+cljs (:require-macros [cemerick.cljs.test
                           :refer (is deftest with-test run-tests testing test-var)])
  #+cljs (:require [cemerick.cljs.test :as t]
                   [tesseract.attrs :as attrs :include-macros true]
                   [tesseract.env :as env]
                   [tesseract.cursor]
                   [tesseract.dom :as dom]))
(defn attr=
  "Compare attribute values to check equality"
  [& attrvals]
  (apply = (map #(set (clojure.string/split % #"\s")) attrvals)))

(deftest to-attr
  (testing "Keywords"
    (is (attr= "value"
               (attrs/to-attr :value))))
  (testing "Strings"
    (is (attr= "attribute-value"
               (attrs/to-attr "attribute-value"))))
  (testing "Numbers"
    (is (= "1" (attrs/to-attr 1))
        (= "2.31" (attrs/to-attr 2.31))))
  (testing "Lists"
    (is (attr= "first second third"
               (attrs/to-attr (list :first "second" "third")))))
  (testing "Vectors"
    (is (attr= "first second third"
               (attrs/to-attr ["first" "second" "third"])))
    (is (attr= "keyword second third"
               (attrs/to-attr [:keyword "second" "third"])))
    (is (attr= "hash-one hash-two second keyword"
               (attrs/to-attr [{:hash-one true
                               :hash-two true}
                              "second"
                              :keyword]))))
  (testing "Hash maps"
    (is (attr= "first second"
               (attrs/to-attr {"first" true
                              "second" true})))
    (is (attr= "second only"
               (attrs/to-attr {"first" false
                              "second" true
                              "only" true})))
    (is (attr= "vector key other"
               (attrs/to-attr {["vector" "key"] true
                              "other" true})))
    (is (attr= "keyword other"
               (attrs/to-attr {:keyword true
                              "other" true}))))
  (testing "Sets"
    (is (attr= "first second"
               (attrs/to-attr #{:first "second"}))))
  (testing "Sorted map"
    ; attribute value should come out in the sorted order
    (is (= "first second"
           (attrs/to-attr (sorted-map-by
                           #(compare (name %1) (name %2))
                           :first true "second" true :third false)))))
  (testing "Sorted set"
    ; attribute value should come out in the sorted order
    (is (= "first second"
           (attrs/to-attr (sorted-set-by
                           compare
                           :first :second))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-attrs-diff
  (testing "no difference"
    (let [a {:class :some-class :id :some-id}]
      (is (nil? (seq (attrs/attrs-diff a a))))))
  (let [cases [[{:a 1}
                {:a 1 :b 1}
                [[:set-attr :b 1]]]
               [{:a 1 :b 1}
                {:a 1}
                [[:remove-attr :b]]]
               [{:a 1}
                {:b 1}
                [[:set-attr :b 1] [:remove-attr :a]]]
               [{:style {:height 1}}
                {:style {:height 2}}
                [[:set-style :height 2]]]
               [{:style {:height 1}}
                {:style {:height 1 :width 2}}
                [[:set-style :width 2]]]
               [{:style {:height 1 :width 1}}
                {:style {:height 2 :width 2}}
                [[:set-style :width 2] [:set-style :height 2]]]
               [{:class [:a :b]}
                {:class [:a]}
                [[:set-attr :class [:a]]]]]]
    (doseq [[prev next expected] cases]
      (is (= (set expected) (set (attrs/attrs-diff prev next)))))))

(deftest test-build-attrs!
  (is (= {:class "foo bar"}
         (-> (dom/div {:on-click (fn [e] nil)
                       :class [:foo :bar]})
             (attrs/build-attrs! nil nil)
             (attrs/get-attrs)))))

(deftest test-handler-registration
  (let [cursor (tesseract.cursor/->cursor :root-id 0 0)
        env (env/create-env)
        event-name :click
        handler (fn [e c])
        ks [:handlers event-name cursor]]
    (attrs/register-handler! env event-name cursor handler)
    (is (= handler (attrs/get-handler env event-name cursor)))
    (attrs/unregister-handler! env event-name cursor)
    (is (nil? (attrs/get-handler env event-name cursor)))))

(deftest test-attr-env
  (let [env (env/create-env)
        cursor (tesseract.cursor/->cursor :root-id 1)
        component (-> (dom/div {}) (tesseract.cursor/assoc-cursor cursor))
        handler (fn [e c])]
    (testing "adding handler"
      (attrs/build-attr! {} component :on-click handler nil env)
      (is (= handler (attrs/get-handler env :click cursor))))
    (testing "removing handler"
      (attrs/build-attr! {} component :on-click nil handler env)
      (is (nil? (attrs/get-handler env :click cursor))))))
