(ns tesseract.impl.vdom-test
  #+cljs
  (:require-macros
    [cemerick.cljs.test :refer [is are deftest testing]])
  (:require
    #+clj  [clojure.test :as t :refer [is are deftest testing]]
    #+cljs [cemerick.cljs.test :as t]
    [tesseract.impl.vdom :as vdom]))
