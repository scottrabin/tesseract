(ns tesseract.core-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.dom :as dom]))

(defcomponent Test
  (render [component]
          (dom/ul {:class :test-component}
                  (for [i (range 10)]
                    (dom/li {} (str "Number " i))))))

(deftest defines-convenience-constructor
  (is (ifn? Test)))

(deftest test-render
  (let [component (Test {})
        out (core/render component)]
    (is (= out (dom/ul {:class :test-component}
                       (for [i (range 10)]
                         (dom/li {} (str "Number " i))))))))

