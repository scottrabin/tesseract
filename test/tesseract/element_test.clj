(ns tesseract.element-test
  (:require [clojure.test :refer :all]
            [tesseract.element :as elem]))

(deftest defelement
  (let [div (elem/defelement div)]
    (testing "defined value")
      (is (ifn? div))))

(deftest element-render
  (testing "rendered elements"
    (let [div (elem/defelement div)
          element (div {:class "some-class"
                        :data-attr "data attribute"})]
      (is (= element {:tag :div
                      :attrs {:class "some-class"
                              :data-attr "data attribute"}
                      :children []})))))
