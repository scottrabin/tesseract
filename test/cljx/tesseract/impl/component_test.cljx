(ns tesseract.impl.component-test
  #+cljs
  (:require-macros
    [cemerick.cljs.test :refer [deftest testing is are]])
  (:require
    #+clj  [clojure.test :refer [deftest testing is are]]
    #+cljs [cemerick.cljs.test :as t]
    #+clj  [tesseract.core]
    #+cljs [tesseract.core :include-macros true]
    [tesseract.impl.component :as component]
    [tesseract.dom :as dom]))

(tesseract.core/defcomponent Attrs
  (render [attrs _ _]
          (dom/div attrs)))

(tesseract.core/defcomponent State
  (initial-state {:stateful? true})
  (render [_ state _]
          (dom/span nil (pr-str state))))

(tesseract.core/defcomponent NoState
  (render [_ _ _]
          (dom/span nil "no state")))

(tesseract.core/defcomponent Children
  (render [_ _ children]
          (apply dom/div nil children)))

(deftest defcomponent
  (testing "exposes a convenience method and a parent class type"
    (are [f] (ifn? f)
         Attrs
         State
         NoState
         Children)
    (are [c] (not (nil? c))
         AttrsComponent
         StateComponent
         NoStateComponent
         ChildrenComponent))
  (testing "object class implements Component protocol"
    (are [c] (satisfies? component/IComponent (c))
         Attrs
         State
         NoState
         Children))
  (testing "convenience function"
    (testing "0-arity"
      (are [a] (identical? (a) (a))
           Attrs
           State
           NoState
           Children))
    (testing "1-arity"
      (are [a attrs] (= attrs (:attrs (a attrs)))
           Attrs {:attrs true :state false :children false}
           State {:attrs false :state true :children false}
           NoState {:attrs false :state false :children false}
           Children {:attrs false :state false :children true}))
    (testing "2+-arity"
      (doseq [c [Attrs State NoState Children]]
        (let [attrs {:name (name (c))}
              one-child (c attrs (dom/div nil "ok"))
              two-child (c attrs (dom/span nil "one") (dom/span nil "two"))]
          (is (= (seq [(dom/div nil "ok")])
                 (seq (:children one-child)))
          (is (= (seq [(dom/span nil "one") (dom/span nil "two")])
                 (seq (:children two-child)))))))))
  (testing "#name"
    (are [c n] (= (name (c)) n)
         Attrs "Attrs"
         State "State"
         NoState "NoState"
         Children "Children"))
  (testing "IComponent#render"
    (testing "returns body of render method"
      (are [c r] (= (component/render c) r)
           (Attrs {:data-test "ok"}) (dom/div {:data-test "ok"})
           (State nil) (dom/span nil (pr-str {:stateful? true}))
           (NoState nil) (dom/span nil "no state")
           (Children nil
                     (dom/div {:first true})
                     (dom/span {:second true}))
           (dom/div nil
                    (dom/div {:first true})
                    (dom/span {:second true})))))
  (testing "Object#toString"
    (are [c st] (= (str c) st)
         (Attrs {:data-str-test :yes})
         "<div data-str-test=\"yes\"></div>"

         (State nil)
         "<span>{:stateful? true}</span>"

         (NoState)
         "<span>no state</span>"

         (Children nil
                   (dom/div {:first :true} "div contents")
                   (dom/span {:2nd "yes"} "span contents"))
         "<div><div first=\"true\">div contents</div><span 2nd=\"yes\">span contents</span></div>")))
