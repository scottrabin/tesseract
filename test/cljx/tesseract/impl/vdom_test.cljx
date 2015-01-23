(ns tesseract.impl.vdom-test
  #+cljs
  (:require-macros
    [cemerick.cljs.test :refer [is are deftest testing]])
  (:require
    #+clj  [clojure.test :as t :refer [is are deftest testing]]
    #+cljs [cemerick.cljs.test :as t]
    [tesseract.impl.patch :as impl.patch]
    [tesseract.impl.vdom :as impl.vdom]
    [render-medium :refer [->VirtualNode ->RenderNode ->PatchAttributes]]))

(deftest vdom-diff
  (testing "when two nodes are of the same type"
    (testing "when the nodes have the same attributes"
      (is (= nil
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1 :attr-two 2})
               (->VirtualNode {:attr-one 1 :attr-two 2})))))

    (testing "when the nodes have different values for the same attributes"
      (is (= (->PatchAttributes {:attr-two 3})
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1 :attr-two 2})
               (->VirtualNode {:attr-one 1 :attr-two 3})))))

    (testing "when the nodes have different attributes"
      (is (= (->PatchAttributes {:attr-one nil :attr-two 2})
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1})
               (->VirtualNode {:attr-two 2})))))

    (testing "no children"
      (testing "when the nodes have the same attributes"
        (is (= nil
               (impl.vdom/diff
                 (->VirtualNode {:attr-one 1 :attr-two 2})
                 (->VirtualNode {:attr-one 1 :attr-two 2})))))
      (testing "when the nodes have different values for the same attributes"
        (is (= (->PatchAttributes {:attr-two 3})
               (impl.vdom/diff
                 (->VirtualNode {:attr-one 1 :attr-two 2})
                 (->VirtualNode {:attr-one 1 :attr-two 3})))))
      (testing "when the nodes have different attributes"
        (is (= (->PatchAttributes {:attr-one nil :attr-two 2})
               (impl.vdom/diff
                 (->VirtualNode {:attr-one 1})
                 (->VirtualNode {:attr-two 2}))))))

    (testing "one child, unchanged"
      (is (= nil
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2}))))))

    (testing "two children, unchanged"
      (is (= nil
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))))))

    (testing "one child, changed"
      (is (= (impl.vdom/->PatchChild 0 (->PatchAttributes {:attr-two 3}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 3}))))))

    (testing "two children, first modified"
      (is (= (impl.vdom/->PatchChild 0 (->PatchAttributes {:attr-two 3}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-two 2}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 3})
                              (->VirtualNode {:attr-two 2}))))))

    (testing "two children, both modified"
      (is (= (impl.patch/combine
               (impl.vdom/->PatchChild 0 (->PatchAttributes {:attr-two 3}))
               (impl.vdom/->PatchChild 1 (->PatchAttributes {:attr-two 4})))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-two 2}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 3})
                              (->VirtualNode {:attr-two 4}))))))

    (testing "no children, append first"
      (is (= (impl.vdom/->PatchMount 0 (->VirtualNode {:attr-two 2}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1})
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2}))))))

    (testing "one child, append second"
      (is (= (impl.vdom/->PatchMount 1 (->VirtualNode {:attr-three 3}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))))))

    (testing "two children, remove second"
      (is (= (impl.vdom/->PatchUnmount 1 (->VirtualNode {:attr-three 3}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2}))))))

    (testing "two children, remove first"
      ;; TODO figure out how to reorder children so the patch is actually
      ;; (impl.vdom/->PatchUnmount 1 (->VirtualNode {:attr-three 3}))
      (is (= (impl.patch/combine
               (impl.vdom/->PatchChild 0 (->PatchAttributes {:attr-two nil
                                                             :attr-three 3}))
               (impl.vdom/->PatchUnmount 1 (->VirtualNode {:attr-three 3})))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-three 3}))))))


    (testing "two children, replace first with nil"
      (is (= (impl.vdom/->PatchUnmount 0 (->VirtualNode {:attr-two 2}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))
               (->VirtualNode {:attr-one 1}
                              nil
                              (->VirtualNode {:attr-three 3}))))))

    (testing "two children, replace second with nil"
      (is (= (impl.vdom/->PatchUnmount 1 (->VirtualNode {:attr-three 3}))
             (impl.vdom/diff
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              (->VirtualNode {:attr-three 3}))
               (->VirtualNode {:attr-one 1}
                              (->VirtualNode {:attr-two 2})
                              nil)))))

    (testing "nested children"
      (testing "modifying a grandchild"
        (is (= (impl.vdom/->PatchChild
                 0
                 (impl.vdom/->PatchChild
                   0
                   (->PatchAttributes {:attr-one 2})))
               (impl.vdom/diff
                 (->VirtualNode
                   {:level 1}
                   (->VirtualNode
                     {:level 2}
                     (->VirtualNode {:attr-one 1})))
                 (->VirtualNode
                   {:level 1}
                   (->VirtualNode
                     {:level 2}
                     (->VirtualNode {:attr-one 2})))))))
      (testing "inserting a grandchild"
        (is (= (impl.vdom/->PatchChild
                 0
                 (impl.vdom/->PatchMount 0 (->VirtualNode {:attr-one 1})))
               (impl.vdom/diff
                 (->VirtualNode
                   {:level 1}
                   (->VirtualNode
                     {:level 2}))
                 (->VirtualNode
                   {:level 1}
                   (->VirtualNode
                     {:level 2}
                     (->VirtualNode {:attr-one 1})))))))
      (testing "removing a grandchild"
        (is (= (impl.vdom/->PatchChild
                 0
                 (impl.vdom/->PatchUnmount 0 (->VirtualNode {:attr-one 1})))
               (impl.vdom/diff
                 (->VirtualNode
                   {:level 1}
                   (->VirtualNode
                     {:level 2}
                     (->VirtualNode {:attr-one 1})))
                 (->VirtualNode
                   {:level 1}
                   (->VirtualNode
                     {:level 2}))))))))
  (testing "when two nodes are of different types"
    ;; TODO
    ))

(deftest patch-mount
  (testing "calls `-insert!` on the given node with the mount node"
    (let [patch (impl.vdom/->PatchMount 1 (->VirtualNode {:attr-one 1}))
          node (->RenderNode)]
      (impl.patch/patch! node [patch])
      (is (= [[1 (->VirtualNode {:attr-one 1})]] @(:insert-calls node)))
      (is (= [] @(:remove-calls node)))
      (is (= [] @(:patch-calls node))))))

(deftest patch-unmount
  (testing "calls `-remove!` on the given node with the node at the given position"
    (let [child-node (->RenderNode {:child "true"})
          parent-node (->RenderNode {:parent "true"} [child-node])
          patch (impl.vdom/->PatchUnmount 0 (->VirtualNode {:child "true"}))]
      (impl.patch/patch! parent-node [patch])
      (is (= [] @(:insert-calls parent-node)))
      (is (= [[0 (->VirtualNode {:child "true"})]] @(:remove-calls parent-node)))
      (is (= [] @(:patch-calls parent-node)))
      (is (= [] @(:insert-calls child-node)))
      (is (= [] @(:remove-calls child-node)))
      (is (= [] @(:patch-calls child-node))))))

(deftest patch-child
  (testing "applies itself to the correct child node"
    (let [child1-node (->RenderNode {:child 1})
          child2-node (->RenderNode {:child 2})
          parent-node (->RenderNode {:parent "true"} [child1-node child2-node])
          child-patch (impl.vdom/diff (->VirtualNode {:child 2})
                                      (->VirtualNode {:child "true"}))
          patch (impl.vdom/->PatchChild 1 child-patch)]
      (impl.patch/patch! parent-node [patch])
      (is (= [] @(:insert-calls parent-node)))
      (is (= [] @(:remove-calls parent-node)))
      (is (= [] @(:patch-calls parent-node)))
      (is (= [] @(:insert-calls child1-node)))
      (is (= [] @(:remove-calls child1-node)))
      (is (= [] @(:patch-calls child1-node)))
      (is (= [] @(:insert-calls child2-node)))
      (is (= [] @(:remove-calls child2-node)))
      (is (= [child-patch] @(:patch-calls child2-node))))))
