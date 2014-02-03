(ns tesseract.element-test
  #+clj (:require [tesseract.element :as elem :refer [defelement]]
                  [tesseract.impl :refer [render]]
                  [clojure.test :refer :all])
  #+cljs (:require [tesseract.element :as elem]
                   [tesseract.impl :refer [render]]
                   [cemerick.cljs.test :as t])
  #+cljs (:require-macros [tesseract.element :refer [defelement]]
                          [cemerick.cljs.test :refer [is deftest testing]]))

(deftest test-defelement
  (let [div (defelement div)]
    (testing "defined value"
      (is (ifn? div)))))

(deftest element-to-string
  (testing "rendered elements"
    (let [div (defelement div)
          span (defelement span)]
      (is (= "<div></div>"
             (str (div {}))))
      (is (= "<div class=\"some-class\"></div>"
             (str (div {:class "some-class"}))))
      (is (= "<div class=\"parent\"><span class=\"child\"></span></div>"
             (str (div
                    {:class "parent"}
                    (span
                      {:class "child"})))))
      (is (= "<div>Arbitrary text here</div>"
             (str (div
                    {}
                    "Arbitrary text here")))))))
