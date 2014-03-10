(ns tesseract.component-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.component :as component]
            [tesseract.dom :as dom]))

(defcomponent OrderedList
  (will-build! [this next-component])
  (did-build! [this prev-component root-node])
  (will-mount! [this] this)
  (did-mount! [this root-node])
  (render [component]
          (apply dom/ol
                 {:class :test-component}
                 (for [child (:children component)]
                   (dom/li {} child)))))

(deftest defines-convenience-constructor
  (is (ifn? OrderedList)))

(deftest satisfies-protocols
  (let [c (OrderedList {})]
    (is (satisfies? component/IComponent c))
    (is (satisfies? component/IBuiltComponent c))
    (is (satisfies? component/IWillBuild c))
    (is (satisfies? component/IDidBuild c))
    (is (satisfies? component/IWillMount c))
    (is (satisfies? component/IDidMount c))))

(deftest test-render
  (let [c (OrderedList {} "first" "second")]
    (is (= (dom/ol {:class :test-component}
                   (dom/li {} "first")
                   (dom/li {} "second"))
           (component/render c)))))

(deftest test-IBuiltComponent-protocol
  (testing "assoc-children"
    (let [c (OrderedList {})
          children [(dom/div {})]]
      (is (= children
             (-> (component/-assoc-children c children)
                 (component/-get-children))))))
  (let [li0 (dom/li {} "zero")
        li1 (dom/li {} "one")
        ol (-> (dom/ol {} li0 li1)
               (component/assoc-child 0 li0)
               (component/assoc-child 1 li1))
        c (-> (OrderedList {} li0 li1)
              (component/assoc-child 0 ol))]
    (testing "get-children"
      (let [children (component/get-children c)]
        (is (vector? children))
        (is (= [ol]))))
    (testing "get-child"
      (is (= ol (component/get-child c 0))))
    (testing "get-child-in (depth:1)"
      (is (= li0 (component/get-child-in c [0 0])))
      (is (= li1 (component/get-child-in c [0 1]))))
    (testing "get-child-in (depth:2)"
      (let [parent-li (-> (dom/li {} c)
                          (component/assoc-child 0 c))
            parent-ol (-> (dom/ol {} parent-li)
                          (component/assoc-child 0 parent-li))
            parent-c (-> (OrderedList {} c)
                         (component/assoc-child 0 parent-ol))]
        (is (= parent-ol (component/get-child parent-c 0)))
        (is (= parent-li (component/get-child-in parent-c [0 0])))
        (is (= c (component/get-child-in parent-c [0 0 0])))
        (is (= ol (component/get-child-in parent-c [0 0 0 0])))
        (is (= li0 (component/get-child-in parent-c [0 0 0 0 0])))
        (is (= li1 (component/get-child-in parent-c [0 0 0 0 1])))))
    ))

(deftest test-attach!
  (let [c (OrderedList {} "first" "second")]
    (core/attach! c js/document.body)
    (is (= js/document.body.children.length 1))
    (is (= js/document.body.firstChild.nodeName "OL"))
    (is (= js/document.body.firstChild.children.length 2))
    (is (= "first" (.-textContent (aget js/document.body.firstChild.children 0))))
    (is (= "second" (.-textContent (aget js/document.body.firstChild.children 1))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

