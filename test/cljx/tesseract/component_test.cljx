(ns tesseract.component-test
  #+clj (:require [clojure.test :refer [is deftest testing]]
                  [tesseract.core :refer [defcomponent]]
                  [tesseract.dom :as dom]
                  [tesseract.component :as component]
                  [tesseract.component.core])
  #+cljs (:require [tesseract.component :as component]
                   [tesseract.dom :as dom]
                   [tesseract.component.core]
                   [cemerick.cljs.test :as t])
  #+cljs (:require-macros [tesseract.core :refer [defcomponent]]
                          [cemerick.cljs.test :refer [is deftest testing]]))

(defcomponent Comment
  (default-state
    {:state-class "a-comment"})
  (render [{:keys [state attrs] :as component}]
          (dom/li {:class (str (:state-class state))}
                  (dom/span {:class "author"} (:author attrs))
                  (dom/span {:class "body"} (:body attrs)))))

(defcomponent CommentList
  (render [{:keys [attrs] :as component}]
          (dom/ul {:class :comment-list}
                  (map Comment (:comments attrs)))))

(deftest defcomponent-test
  (testing "Convenience function exists"
    (is (ifn? Comment)))
  (testing "Convenience function returns a component"
    (let [cmpt (Comment {})]
      ; TODO - why does this return false, even though the render methods work?
      ;(is (satisfies? tesseract.component.core/IComponent cmpt))
      )))

(deftest default-state-test
  (testing "Components get the default state on instantiation"
    (let [cmpt (Comment {})]
      (is (= {:state-class "a-comment"}
             (:state cmpt))))))

(deftest render-test
  (testing "Render to string"
    (let [cmpt (Comment {:author "Scott Rabin"
                         :body "This is Scott's comment"})]
      (is (= (str "<li class=\"a-comment\">"
                  "<span class=\"author\">Scott Rabin</span>"
                  "<span class=\"body\">This is Scott's comment</span>"
                  "</li>")
             (str (tesseract.component.core/-render cmpt))))))
  (testing "Recursively rendering children"
    (let [cmpt (CommentList {:comments [{:author "Scott Rabin"
                                         :body "This is Scott's comment"}
                                        {:author "Logan Linn"
                                         :body "This is Logan's comment"}]})]
      (is (= (str "<ul class=\"comment-list\">"
                  "<li class=\"a-comment\">"
                  "<span class=\"author\">Scott Rabin</span>"
                  "<span class=\"body\">This is Scott's comment</span>"
                  "</li>"
                  "<li class=\"a-comment\">"
                  "<span class=\"author\">Logan Linn</span>"
                  "<span class=\"body\">This is Logan's comment</span>"
                  "</li>"
                  "</ul>")
             (str (tesseract.component.core/-render cmpt)))))))