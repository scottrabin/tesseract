(ns tesseract.core-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.dom :as dom]))

(defcomponent NumList
  (render [component]
          (dom/ul {:class :test-component}
                  (for [i (range 10)]
                    (dom/li {} (str "Number " i))))))

(deftest defines-convenience-constructor
  (is (ifn? NumList)))

(deftest test-render
  (let [component (NumList {})
        out (core/render component)]
    (is (= out (dom/ul {:class :test-component}
                       (for [i (range 10)]
                         (dom/li {} (str "Number " i))))))))
(deftest test-attach
  (let [c (NumList {})]
    (core/attach c js/document.body)
    (is (= js/document.body.children.length 1))
    (is (= js/document.body.firstChild.nodeName "UL"))
    (is (= js/document.body.firstChild.children.length 10))
    (doseq [i (range 10)]
      (is (= (.-textContent (aget js/document.body.firstChild.children i))
             (str "Number " i))))))
