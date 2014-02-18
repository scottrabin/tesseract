(ns tesseract.core-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.component :as component]
            [tesseract.dom :as dom]))

(defcomponent NumList
  (will-update [this other])
  (did-update [this other container])
  (will-mount! [this] this)
  (did-mount! [this container])
  (render [component]
    (dom/ul {:class :test-component}
            (for [i (range 10)]
              (dom/li {} (str "Number " i))))))

(deftest defines-convenience-constructor
  (is (ifn? NumList)))

(deftest satisfies-protocols
  (let [c (NumList {})]
    (is (satisfies? component/IComponent c))
    (is (satisfies? component/IWillUpdate c))
    (is (satisfies? component/IDidUpdate c))
    (is (satisfies? component/IWillMount c))
    (is (satisfies? component/IDidMount c))))

(deftest test-render
  (let [component (NumList {})
        out (component/render component)]
    (is (= out (dom/ul {:class :test-component}
                       (for [i (range 10)]
                         (dom/li {} (str "Number " i))))))))
(deftest test-attach!
  (let [c (NumList {})]
    (core/attach! c js/document.body)
    (is (= js/document.body.children.length 1))
    (is (= js/document.body.firstChild.nodeName "UL"))
    (is (= js/document.body.firstChild.children.length 10))
    (doseq [i (range 10)]
      (is (= (.-textContent (aget js/document.body.firstChild.children i))
             (str "Number " i))))))

(defcomponent Comment
  (render [{:keys [attrs children]}]
    (dom/div {:class :comment}
             (dom/h2 {:class :comment-author} (:author attrs))
             children)))

(defcomponent CommentList
  (render [component]
    (dom/div {:class :comment-list}
             (Comment {:author "Logan Linn"} "This is one comment")
             (Comment {:author "Scott Rabin"} "This is *another* comment"))))

(deftest test-comment-list
  (let [comment-list (CommentList {})
        out (component/render comment-list)]
    (is (= :div (:tag out)))
    (is (= :comment-list (-> out :attrs :class)))
    (is (= 2 (count (:children out))))
    (is (= "<div class=\"comment-list\"><div class=\"comment\"><h2 class=\"comment-author\">Logan Linn</h2>This is one comment</div><div class=\"comment\"><h2 class=\"comment-author\">Scott Rabin</h2>This is *another* comment</div></div>"
           (str out)))))
