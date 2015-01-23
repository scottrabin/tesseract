(ns tesseract.impl.component-test
  #+cljs
  (:require-macros
    [cemerick.cljs.test :refer [deftest testing is are]])
  (:require
    #+clj  [clojure.test :refer [deftest testing is are]]
    #+cljs [cemerick.cljs.test :as t]
    #+clj  [tesseract.core]
    #+cljs [tesseract.core :include-macros true]
    [tesseract.impl.vdom :as impl.vdom]
    [tesseract.impl.component :as impl.component]
    [render-medium :refer [->VirtualNode ->RenderNode]]))

(tesseract.core/defcomponent Attrs
  (render [attrs _ _]
          (->VirtualNode attrs)))

(tesseract.core/defcomponent State
  (initial-state {:stateful? true})
  (render [_ state _]
          (->VirtualNode nil (pr-str state))))

(tesseract.core/defcomponent NoState
  (render [_ _ _]
          (->VirtualNode nil "no state")))

(tesseract.core/defcomponent Children
  (render [_ _ children]
          (apply ->VirtualNode nil children)))

(deftest defcomponent
  (testing "defined symbol is identical to provided symbol"
    (are [f] (ifn? f)
         Attrs
         State
         NoState
         Children))

  (testing #+clj "clojure.lang.IFn" #+cljs "cljs.core/IFn"
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
      (doseq [c [Attrs State NoState Children]
              ;; cljs.core/IFn only goes up to 21 args, no rest-args
              ;; TODO eventually support some number greater than 3...
              num-children (range 1 4)]
        (let [attrs {:name (name (c))}
              children (for [i (range num-children)] (->VirtualNode nil (str i)))
              component (apply c attrs children)]
          (is (= (seq children)
                 (seq (:children component))))))))

  (testing #+clj "clojure.lang.Named" #+cljs "cljs.core/INamed"
    (testing #+clj "#getName" #+cljs "-name"
      (are [c n] (= (name (c)) n)
           Attrs "Attrs"
           State "State"
           NoState "NoState"
           Children "Children")))

  (testing "impl.vdom/IVirtualNode"
    (testing "object class implements protocol"
      (are [c] (satisfies? impl.vdom/IVirtualNode (c))
           Attrs
           State
           NoState
           Children))

    (testing "#-mount!"
      (let [attrs-c (Attrs {:data-test "ok"})
            node (impl.vdom/-mount! attrs-c)]
        (is (= (->RenderNode {:data-test "ok"})
               node))))
    (testing "#-unmount!"
      (let [child-c (->VirtualNode {:child true})
            parent-c (Children {:data-test "ok"} child-c)
            node (impl.vdom/-mount! parent-c)]
        (impl.vdom/-unmount! parent-c node)

        (is (= [[child-c (first (impl.vdom/children node))]]
               @(:unmount-calls child-c)))))
    (testing "#-diff"
      ;; TODO
      )
    (testing "#render"
      (are [c r] (= (impl.vdom/render c) r)
           (Attrs {:data-test "ok"}) (->VirtualNode {:data-test "ok"})
           (State nil) (->VirtualNode nil (pr-str {:stateful? true}))
           (NoState nil) (->VirtualNode nil "no state")
           (Children nil
                     (->VirtualNode {:first true})
                     (->VirtualNode {:second true}))
           (->VirtualNode nil
                          (->VirtualNode {:first true})
                          (->VirtualNode {:second true})))))

  (testing "impl.vdom/IContainerNode"
    (testing "#children"

      )
    )

  (testing "Object"
    (testing "#toString"
      (are [c st] (= st (str c))
           (Attrs {:data-str-test :yes})
           "<node data-str-test=\"yes\"></node>"

           (State nil)
           "<node>{:stateful? true}</node>"

           (NoState)
           "<node>no state</node>"

           (Children nil
                     (->VirtualNode {:first :true} "node contents")
                     (->VirtualNode {:2nd "yes"} "node contents"))
           "<node><node first=\"true\">node contents</node><node 2nd=\"yes\">node contents</node></node>"))))
