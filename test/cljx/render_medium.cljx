(ns render-medium
  "Contains types used in testing to mimic a render medium (e.g. the DOM)
   without requiring the environment to provide it."
  (:require
    [tesseract.attrs :as attrs]
    [tesseract.impl.vdom :as impl.vdom]
    [tesseract.impl.patch :as impl.patch]))

(defrecord PatchAttributes [attrs]
  impl.patch/IPatch
  (-patch! [this node]
    (swap! (:patch-calls node) conj this)))

(deftype RenderNode [attrs children insert-calls remove-calls patch-calls]
  Object
  (toString [this]
    (str "{:attrs " (pr-str @attrs) " :children " (pr-str @children) "}"))
  #+cljs IEquiv
  (#+clj equals #+cljs -equiv
    [_ other]
    (and (= @attrs @(.-attrs other))
         (= @children @(.-children other))))

  #+clj clojure.lang.ILookup #+cljs ILookup
  (#+clj valAt #+cljs -lookup
    [this k]
    (#+clj .valAt #+cljs -lookup this k nil))
  (#+clj valAt #+cljs -lookup
    [this k not-found]
    (case k
      :attrs         attrs
      :children      children
      :insert-calls  insert-calls
      :remove-calls  remove-calls
      :patch-calls   patch-calls

      not-found))

  impl.vdom/IRenderNode
  (-insert! [this insert-vnode insert-position]
    (swap! insert-calls conj [insert-position insert-vnode])
    (swap! children #(vec (concat
                            (take insert-position %)
                            [(impl.vdom/-mount! insert-vnode)]
                            (drop insert-position %)))))
  (-remove! [this remove-vnode remove-position]
    (swap! remove-calls conj [remove-position remove-vnode])
    (swap! children #(vec (concat
                            (take remove-position %)
                            (drop (inc remove-position) %)))))

  impl.vdom/IContainerNode
  (children [_]
    @children))

(defn ->RenderNode
  ([]
   (->RenderNode {} nil))
  ([attrs]
   (->RenderNode attrs nil))
  ([attrs children]
   (new RenderNode (atom attrs) (atom (vec children)) (atom []) (atom []) (atom []))))

(deftype VirtualNode [attrs children mount-calls unmount-calls]
  Object
  (toString [_]
    (str
      "<node"
      (when (seq attrs)
        (->> attrs
             (filter #(satisfies? attrs/IAttributeValue (val %)))
             (clojure.core/map attrs/to-element-attribute)
             (clojure.string/join " ")
             (str " ")))
      ">"
      (when (seq children)
        (clojure.string/join (clojure.core/map str (flatten children))))
      "</node>"))
  #+cljs IEquiv
  (#+clj equals #+cljs -equiv
    [_ other]
    (and (= attrs (.-attrs other))
         (= children (.-children other))))

  #+clj clojure.lang.ILookup #+cljs ILookup
  (#+clj valAt #+cljs -lookup
    [this k]
    (#+clj .valAt #+cljs -lookup this k nil))
  (#+clj valAt #+cljs -lookup
    [this k not-found]
    (case k
      :attrs         attrs
      :children      children
      :mount-calls   mount-calls
      :unmount-calls unmount-calls

      not-found))

  impl.vdom/IVirtualNode
  (-mount! [this]
    (swap! mount-calls conj this)
    (->RenderNode attrs (map impl.vdom/-mount! children)))
  (-unmount! [this node]
    (swap! unmount-calls conj [this node])
    (doseq [i (range (count children))]
      (impl.vdom/-unmount! (nth children i) (nth (impl.vdom/children node) i))))
  (-diff [this other]
    (when-let [diff-attrs (impl.vdom/diff-map attrs (:attrs other))]
      (->PatchAttributes diff-attrs)))
  (render [this]
    this)

  impl.vdom/IContainerNode
  (children [this]
    children))

(defn ->VirtualNode
  ([attrs]
   (new VirtualNode attrs nil (atom []) (atom [])))
  ([attrs & children]
   (new VirtualNode attrs (not-empty (vec children)) (atom []) (atom []))))
