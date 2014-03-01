(ns tesseract.attrs-test
  #+clj (:require [clojure.test :refer :all]
                  [tesseract.attrs :as attrs]
                  [tesseract.dom :as dom])
  #+cljs (:require-macros [cemerick.cljs.test
                           :refer (is deftest with-test run-tests testing test-var)])
  #+cljs (:require [cemerick.cljs.test :as t]
                   [tesseract.attrs :as attrs]
                   [tesseract.dom :as dom]))

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

(deftest test-build-attrs
  (is (= {:class "foo bar"}
         (attrs/build-attrs
           (dom/div {:on-click (fn [e] nil)
                     :class [:foo :bar]})))))
