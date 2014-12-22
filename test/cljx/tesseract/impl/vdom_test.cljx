(ns tesseract.impl.vdom-test
  #+cljs
  (:require-macros
    [cemerick.cljs.test :refer [is are deftest testing]])
  (:require
    #+clj  [clojure.test :refer [deftest testing is are]]
    #+cljs [cemerick.cljs.test :as t]
    [tesseract.impl.vdom :as vdom]
    [tesseract.dom :as dom]))

(deftest diff-test
  (testing "single DOM elements"
    (are [prev-node next-node expected-patches]
         (= expected-patches (vdom/diff prev-node next-node))

         ;; no changes
         (dom/div {:data-something "not changed"})
         (dom/div {:data-something "not changed"})
         []

         ;; same map, value changed
         (dom/div {:data-something "change me"})
         (dom/div {:data-something "changed"})
         [(vdom/->SetAttributes {"data-something" "changed"})]

         ;; different map, new value
         (dom/div {:data-something "keep me"})
         (dom/div {:data-something "keep me"
                   :data-other "new"})
         [(vdom/->SetAttributes {"data-other" "new"})]

         ;; different map, missing value
         (dom/div {:data-something "remove-me"})
         (dom/div {})
         [(vdom/->SetAttributes {"data-something" nil})]

         ;; different map, value added & value removed
         (dom/div {:data-remove-me "yes"})
         (dom/div {:data-add-me "added"})
         [(vdom/->SetAttributes {"data-remove-me" nil
                                 "data-add-me" "added"})]

         )))
