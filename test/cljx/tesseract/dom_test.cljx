(ns tesseract.dom-test
  #+cljs
  (:require-macros
    [cemerick.cljs.test :refer [is are deftest testing]])
  (:require
    #+clj  [tesseract.dom :as dom :refer [defelement]]
    #+cljs [tesseract.dom :as dom :refer-macros [defelement]]
    #+clj  [clojure.test :as t :refer [is are deftest testing]]
    #+cljs [cemerick.cljs.test :as t]
    [clojure.string :as str]
    [tesseract.impl.vdom :as impl.vdom]))

(defn- nodename=
  "Compare a node's tag name to the given name (with optional namespace)"
  [nodename node]
  (= (str/lower-case nodename) (str/lower-case (.-nodeName node))))

(defrecord IntermediateElement [actual-element mount-calls unmount-calls]
  impl.vdom/IVirtualNode
  (-mount! [this]
    (swap! mount-calls conj this)
    (impl.vdom/-mount! actual-element))
  (-unmount! [this node]
    (swap! unmount-calls conj [this node])
    (impl.vdom/-unmount! actual-element node))
  (-diff [this next-node]
    (impl.vdom/diff actual-element (:actual-element next-node)))
  (render [this]
    (impl.vdom/render actual-element)))

(defn- ->IntermediateElement
  [actual-element]
  (new IntermediateElement actual-element (atom []) (atom [])))

(defelement my-element)

(deftest test-defelement
  (testing "defined value"
    (is (ifn? my-element))
    (testing "0-arity"
      (is (identical? (my-element) (my-element))))
    (testing "1-arity"
      (is (identical? (my-element nil) (my-element nil)))
      (is (identical? (my-element) (my-element nil)))
      (is (= {:data-attr true} (:attrs (my-element {:data-attr true})))))
    (testing "2-arity"
      (let [el (my-element {:data-attr "ok"}
                           (dom/div {:id "one"})
                           (dom/span {:id "two"}))]
        (is (= {:data-attr "ok"} (:attrs el)))
        (is (= (dom/div {:id "one"}) (get-in el [:children 0])))
        (is (= (dom/span {:id "two"}) (get-in el [:children 1]))))))
  (testing "render to string"
    (is (= "<my-element data-attr=\"some value\"></my-element>"
           (str (my-element {:data-attr "some value"}))))))

; Test each element individually; the plan will have some individual logic
; build into certain elements, and it makes more sense to test each one
; individually for now.
(deftest test-element-a
  (is (ifn? dom/a)))
(deftest test-element-abbr
  (is (ifn? dom/abbr)))
(deftest test-element-address
  (is (ifn? dom/address)))
(deftest test-element-area
  (is (ifn? dom/area)))
(deftest test-element-article
  (is (ifn? dom/article)))
(deftest test-element-aside
  (is (ifn? dom/aside)))
(deftest test-element-audio
  (is (ifn? dom/audio)))
(deftest test-element-b
  (is (ifn? dom/b)))
(deftest test-element-base
  (is (ifn? dom/base)))
(deftest test-element-bdi
  (is (ifn? dom/bdi)))
(deftest test-element-bdo
  (is (ifn? dom/bdo)))
(deftest test-element-blockquote
  (is (ifn? dom/blockquote)))
(deftest test-element-body
  (is (ifn? dom/body)))
(deftest test-element-br
  (is (ifn? dom/br)))
(deftest test-element-button
  (is (ifn? dom/button)))
(deftest test-element-canvas
  (is (ifn? dom/canvas)))
(deftest test-element-caption
  (is (ifn? dom/caption)))
(deftest test-element-cite
  (is (ifn? dom/cite)))
(deftest test-element-code
  (is (ifn? dom/code)))
(deftest test-element-col
  (is (ifn? dom/col)))
(deftest test-element-colgroup
  (is (ifn? dom/colgroup)))
(deftest test-element-data
  (is (ifn? dom/data)))
(deftest test-element-datalist
  (is (ifn? dom/datalist)))
(deftest test-element-dd
  (is (ifn? dom/dd)))
(deftest test-element-del
  (is (ifn? dom/del)))
(deftest test-element-details
  (is (ifn? dom/details)))
(deftest test-element-dfn
  (is (ifn? dom/dfn)))
(deftest test-element-div
  (is (ifn? dom/div)))
(deftest test-element-dl
  (is (ifn? dom/dl)))
(deftest test-element-dt
  (is (ifn? dom/dt)))
(deftest test-element-element
  (is (ifn? dom/element)))
(deftest test-element-em
  (is (ifn? dom/em)))
(deftest test-element-embed
  (is (ifn? dom/embed)))
(deftest test-element-fieldset
  (is (ifn? dom/fieldset)))
(deftest test-element-figcaption
  (is (ifn? dom/figcaption)))
(deftest test-element-figure
  (is (ifn? dom/figure)))
(deftest test-element-footer
  (is (ifn? dom/footer)))
(deftest test-element-form
  (is (ifn? dom/form)))
(deftest test-element-h1
  (is (ifn? dom/h1)))
(deftest test-element-h2
  (is (ifn? dom/h2)))
(deftest test-element-h3
  (is (ifn? dom/h3)))
(deftest test-element-h4
  (is (ifn? dom/h4)))
(deftest test-element-h5
  (is (ifn? dom/h5)))
(deftest test-element-h6
  (is (ifn? dom/h6)))
(deftest test-element-head
  (is (ifn? dom/head)))
(deftest test-element-header
  (is (ifn? dom/header)))
(deftest test-element-hr
  (is (ifn? dom/hr)))
(deftest test-element-html
  (is (ifn? dom/html)))
(deftest test-element-i
  (is (ifn? dom/i)))
(deftest test-element-iframe
  (is (ifn? dom/iframe)))
(deftest test-element-img
  (is (ifn? dom/img)))
(deftest test-element-input
  (is (ifn? dom/input)))
(deftest test-element-ins
  (is (ifn? dom/ins)))
(deftest test-element-kbd
  (is (ifn? dom/kbd)))
(deftest test-element-keygen
  (is (ifn? dom/keygen)))
(deftest test-element-label
  (is (ifn? dom/label)))
(deftest test-element-legend
  (is (ifn? dom/legend)))
(deftest test-element-li
  (is (ifn? dom/li)))
(deftest test-element-link
  (is (ifn? dom/link)))
(deftest test-element-main
  (is (ifn? dom/main)))
(deftest test-element-map
  (is (ifn? dom/map)))
(deftest test-element-mark
  (is (ifn? dom/mark)))
(deftest test-element-menu
  (is (ifn? dom/menu)))
(deftest test-element-menuitem
  (is (ifn? dom/menuitem)))
(deftest test-element-meta
  (is (ifn? dom/meta)))
(deftest test-element-meter
  (is (ifn? dom/meter)))
(deftest test-element-nav
  (is (ifn? dom/nav)))
(deftest test-element-noscript
  (is (ifn? dom/noscript)))
(deftest test-element-object
  (is (ifn? dom/object)))
(deftest test-element-ol
  (is (ifn? dom/ol)))
(deftest test-element-optgroup
  (is (ifn? dom/optgroup)))
(deftest test-element-option
  (is (ifn? dom/option)))
(deftest test-element-output
  (is (ifn? dom/output)))
(deftest test-element-p
  (is (ifn? dom/p)))
(deftest test-element-param
  (is (ifn? dom/param)))
(deftest test-element-pre
  (is (ifn? dom/pre)))
(deftest test-element-progress
  (is (ifn? dom/progress)))
(deftest test-element-q
  (is (ifn? dom/q)))
(deftest test-element-rp
  (is (ifn? dom/rp)))
(deftest test-element-rt
  (is (ifn? dom/rt)))
(deftest test-element-ruby
  (is (ifn? dom/ruby)))
(deftest test-element-s
  (is (ifn? dom/s)))
(deftest test-element-samp
  (is (ifn? dom/samp)))
(deftest test-element-script
  (is (ifn? dom/script)))
(deftest test-element-section
  (is (ifn? dom/section)))
(deftest test-element-select
  (is (ifn? dom/select)))
(deftest test-element-small
  (is (ifn? dom/small)))
(deftest test-element-source
  (is (ifn? dom/source)))
(deftest test-element-span
  (is (ifn? dom/span)))
(deftest test-element-strong
  (is (ifn? dom/strong)))
(deftest test-element-style
  (is (ifn? dom/style)))
(deftest test-element-sub
  (is (ifn? dom/sub)))
(deftest test-element-summary
  (is (ifn? dom/summary)))
(deftest test-element-sup
  (is (ifn? dom/sup)))
(deftest test-element-table
  (is (ifn? dom/table)))
(deftest test-element-tbody
  (is (ifn? dom/tbody)))
(deftest test-element-td
  (is (ifn? dom/td)))
(deftest test-element-textarea
  (is (ifn? dom/textarea)))
(deftest test-element-tfoot
  (is (ifn? dom/tfoot)))
(deftest test-element-th
  (is (ifn? dom/th)))
(deftest test-element-thead
  (is (ifn? dom/thead)))
(deftest test-element-time
  (is (ifn? dom/time)))
(deftest test-element-title
  (is (ifn? dom/title)))
(deftest test-element-tr
  (is (ifn? dom/tr)))
(deftest test-element-track
  (is (ifn? dom/track)))
(deftest test-element-u
  (is (ifn? dom/u)))
(deftest test-element-ul
  (is (ifn? dom/ul)))
(deftest test-element-var
  (is (ifn? dom/var)))
(deftest test-element-video
  (is (ifn? dom/video)))
(deftest test-element-wbr
  (is (ifn? dom/wbr)))

(deftest Element
  (testing "impl.vdom/IVirtualNode"
    #+cljs
    (testing "#-mount!"
      (testing "instantiates a node with proper attributes"
        (let [vnode (dom/div {:children false})
              node (impl.vdom/-mount! vnode)]
          (is (= "false" (.getAttribute node "children")))
          (is (nodename= "div" node))))
      (testing "correctly appends child elements"
        (let [vnode (dom/span {:children true} (dom/hr))
              node (impl.vdom/-mount! vnode)]
          (is (= "true"
                 (.getAttribute node "children")))
          (is (nodename= "span" node))
          (is (nodename= "hr" (aget (.-childNodes node) 0)))))
      (testing "calls `-mount!` on children vnodes"
        (let [child-vnode (->IntermediateElement (dom/hr))
              vnode (dom/span {:children true} child-vnode)
              node (impl.vdom/-mount! vnode)]
          (is (= [child-vnode]
                 @(:mount-calls child-vnode)))
          (is (nodename= "hr" (aget (.-childNodes node) 0))))))

    #+cljs
    (testing "#-unmount!"
      (let [child-1 (->IntermediateElement (dom/p {:index 0}))
            child-2 (->IntermediateElement (dom/span {:index 1}))
            vparent (->IntermediateElement (dom/div {:parent true} child-1 child-2))
            parent-node (impl.vdom/-mount! vparent)]
        (impl.vdom/-unmount! vparent parent-node)
        (is (= [[child-1 (aget (impl.vdom/children parent-node) 0)]]
               @(:unmount-calls child-1)))
        (is (= [[child-2 (aget (impl.vdom/children parent-node) 1)]]
               @(:unmount-calls child-2)))))

    (testing "#-diff"
      (are [prev-node next-node expected-patches]
           (= expected-patches (impl.vdom/-diff prev-node next-node))

           ;; no changes
           (dom/div {:data-something "not changed"})
           (dom/div {:data-something "not changed"})
           nil

           ;; same map, value changed
           (dom/div {:data-something "change me"})
           (dom/div {:data-something "changed"})
           (dom/->PatchSetAttributes {:data-something "changed"})

           ;; different map, new value
           (dom/div {:data-something "keep me"})
           (dom/div {:data-something "keep me"
                     :data-other "new"})
           (dom/->PatchSetAttributes {:data-other "new"})

           ;; different map, missing value
           (dom/div {:data-something "remove-me"})
           (dom/div {})
           (dom/->PatchSetAttributes {:data-something nil})

           ;; different map, value added & value removed
           (dom/div {:data-remove-me "yes"})
           (dom/div {:data-add-me "added"})
           (dom/->PatchSetAttributes {:data-remove-me nil
                                      :data-add-me "added"})))

    (testing "#render"
      ;; TODO
      ))

  (testing "impl.vdom/IContainerNode"
    (testing "#children"
      (is (= nil
             (impl.vdom/children (dom/span))))
      (is (= nil
             (impl.vdom/children (dom/div nil))))
      (is (= nil
             (impl.vdom/children (dom/p {}))))
      (is (= nil
             (impl.vdom/children (dom/section {:attr "value"}))))
      (is (= [(dom/hr)]
             (impl.vdom/children (dom/span nil (dom/hr)))))
      (is (= [(dom/hr) (dom/table)]
             (impl.vdom/children (dom/div nil (dom/hr) (dom/table)))))))

  (testing "Object"
    (testing "#toString"
      (is (= "<div></div>"
             (str (dom/div {}))))
      (is (= "<div class=\"some-class\"></div>"
             (str (dom/div {:class :some-class}))))
      (is (= "<div class=\"parent\"><span class=\"child\"></span></div>"
             (str (dom/div {:class "parent"}
                           (dom/span {:class "child"})))))
      (is (= "<div>Arbitrary text here</div>"
             (str (dom/div {} "Arbitrary text here"))))

      (testing "escaping attributes"
        (is (= "<div class=\"&lt;&gt;&quot;&apos;&amp;\"></div>"
               (str (dom/div {:class "<>\"'&"})))))

      (testing "recurses children"
        (is (= "<div><span>0</span><span>1</span></div>"
               (str (dom/div {} (for [i (range 2)]
                                  (dom/span {} i))))))))))

#+cljs
(deftest extend-basic-types
  (testing "string"
    (let [vnode (dom/span {:children "string"}
                          "This is the first string"
                          "Followed by another string"
                          "And yet another")
          node (impl.vdom/-mount! vnode)]

      (is (= 3 (count (impl.vdom/children node))))
      (is (= "This is the first string"
             (.-textContent (nth (impl.vdom/children node) 0))))
      (is (= "Followed by another string"
             (.-textContent (nth (impl.vdom/children node) 1))))
      (is (= "And yet another"
             (.-textContent (nth (impl.vdom/children node) 2))))))

  (testing "number"
    (let [vnode (dom/span {:children "number"}
                          0 1 2 3)
          node (impl.vdom/-mount! vnode)]

      (is (= 4 (count (impl.vdom/children node))))
      (is (= "0"
             (.-textContent (nth (impl.vdom/children node) 0))))
      (is (= "1"
             (.-textContent (nth (impl.vdom/children node) 1))))
      (is (= "2"
             (.-textContent (nth (impl.vdom/children node) 2))))
      (is (= "3"
             (.-textContent (nth (impl.vdom/children node) 3))))))

  (testing "boolean"
    (let [vnode (dom/span {:children "boolean"}
                          true false)
          node (impl.vdom/-mount! vnode)]

      (is (= 2 (count (impl.vdom/children node))))
      (is (= "true"
             (.-textContent (nth (impl.vdom/children node) 0))))
      (is (= "false"
             (.-textContent (nth (impl.vdom/children node) 1))))))

  (testing "nil"
    (let [vnode (dom/span {:children "boolean"}
                          "OK" nil 3)
          node (impl.vdom/-mount! vnode)]

      (is (= 3 (count (impl.vdom/children node))))
      (is (= "OK"
             (.-textContent (nth (impl.vdom/children node) 0))))
      (is (= (. js/document -COMMENT_NODE)
             (.-nodeType (nth (impl.vdom/children node) 1))))
      (is (= "3"
             (.-textContent (nth (impl.vdom/children node) 2))))
      (is (= "OK3"
             (.-textContent node))))))

#+cljs
(deftest NodeList
  (testing "ISeqable"
    (testing "#-seq"
      (let [child-1 (.createElement js/document "span")
            child-2 (.createElement js/document "p")
            parent  (.createElement js/document "div")]
        (doseq [c [child-1 child-2]]
          (.appendChild parent c))

        (let [nodes (seq (impl.vdom/children parent))]
          (is (= 2 (count nodes)))
          (is (identical? child-1 (first nodes)))
          (is (identical? child-2 (second nodes)))))))

  (testing "ICounted"
    (testing "#-count"
      (let [child-1 (.createElement js/document "span")
            child-2 (.createElement js/document "p")
            child-3 (.createElement js/document "hr")
            parent  (.createElement js/document "div")]
        (doseq [c [child-1 child-2 child-3]]
          (.appendChild parent c))

        (is (= 3 (count (impl.vdom/children parent)))))))

  (testing "IIndexed"
    (testing "#-nth (arity 2)"
      (let [child-1 (.createElement js/document "span")
            child-2 (.createElement js/document "p")
            parent  (.createElement js/document "div")]
        (doseq [c [child-1 child-2]]
          (.appendChild parent c))

        (let [children (impl.vdom/children parent)]
          (is (identical? child-1 (nth children 0)))
          (is (identical? child-2 (nth children 1)))
          (is (nil? (nth children 2))))))
    (testing "#-nth (arity 3)"
      (let [child-1 (.createElement js/document "span")
            child-2 (.createElement js/document "p")
            parent  (.createElement js/document "div")]
        (doseq [c [child-1 child-2]]
          (.appendChild parent c))

        (let [children (impl.vdom/children parent)]
          (is (identical? child-1 (nth children 0 ::not-found)))
          (is (identical? child-2 (nth children 1 ::not-found)))
          (is (= ::not-found (nth children 2 ::not-found))))))))

#+cljs
(deftest HTMLElement
  (testing "impl.vdom/IRenderNode"
    (testing "#-insert!"
      (let [child-1 (.createElement js/document "span")
            child-2 (.createElement js/document "p")
            parent  (.createElement js/document "div")
            to-insert (->IntermediateElement (dom/hr))]
        (doseq [c [child-1 child-2]]
          (.appendChild parent c))
        (impl.vdom/-insert! parent to-insert 1)

        (is (= [to-insert]
               @(:mount-calls to-insert)))
        (is (nodename= "span" (nth (impl.vdom/children parent) 0)))
        (is (nodename= "hr" (nth (impl.vdom/children parent) 1)))
        (is (nodename= "p" (nth (impl.vdom/children parent) 2)))
        (is (= 3 (count (impl.vdom/children parent))))))

    (testing "#-remove!"
      (let [to-remove (->IntermediateElement (dom/hr))
            child-1 (.createElement js/document "span")
            child-2 (impl.vdom/-mount! to-remove)
            child-3 (.createElement js/document "p")
            parent  (.createElement js/document "div")]
        (doseq [c [child-1 child-2 child-3]]
          (.appendChild parent c))
        (impl.vdom/-remove! parent to-remove 1)

        (is (= [[to-remove child-2]]
               @(:unmount-calls to-remove)))
        (is (nodename= "span" (nth (impl.vdom/children parent) 0)))
        (is (nodename= "p" (nth (impl.vdom/children parent) 1)))
        (is (= 2 (count (impl.vdom/children parent)))))))

  (testing "impl.vdom/IContainerNode"
    (testing "#children"
      (let [child-1 (.createElement js/document "span")
            child-2 (.createElement js/document "p")
            parent  (.createElement js/document "div")]
        (doseq [c [child-1 child-2]]
          (.appendChild parent c))

        (is (nodename= "span" (nth (impl.vdom/children parent) 0)))
        (is (nodename= "p" (nth (impl.vdom/children parent) 1)))
        (is (= 2 (count (impl.vdom/children parent))))))))
