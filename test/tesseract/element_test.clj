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
          span (elem/defelement span)]
      (is (= "<div></div>"
             (elem/render (div {}))))
      (is (= "<div class=\"some-class\"></div>"
             (elem/render (div {:class "some-class"}))))
      (is (= "<div class=\"parent\"><span class=\"child\"></span></div>"
             (elem/render (div
                            {:class "parent"}
                            (span
                              {:class "child"})))))
      (is (= "<div>Arbitrary text here</div>"
             (elem/render (div
                            {}
                            "Arbitrary text here")))))))
