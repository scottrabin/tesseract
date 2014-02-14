(ns tesseract.component-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.component :as component]
            [tesseract.dom :as dom]))

(deftest test-attrs-diff
  (testing "no difference"
    (let [a {:class :some-class :id :some-id}]
      (is (nil? (seq (component/attrs-diff a a))))))
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
      (is (= expected (component/attrs-diff prev next))))))
