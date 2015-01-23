(ns tesseract.impl.vdom
  "Diff/patch behavior for renderable nested elements"
  (:require
    [tesseract.impl.patch :as impl.patch]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Protocols

(defprotocol IVirtualNode
  "Virtual nodes are the primary render object in Tesseract. This protocol
  describes all of the core functionality of nodes - how to mount / dismount,
  how to diff against other nodes, and how to render."
  (-mount! [this]
           "Called when node cannot be patched into current state (e.g. because
            it has not yet been created). Returns a non-virtual node that is a
            valid render element.")
  (-unmount! [this node]
             "Called when node no longer exists at the specified point in the
              render tree. Unmounts all children.")
  (-diff [this other]
         "Yields a patch that converts the current node into the other, or nil
          if the nodes are logically equivalent.")
  (render [this]
          "Generate the virtual render tree under this node"))

(defprotocol IRenderNode
  "Render nodes are the terminal nodes that are actually displayed in the
   view medium (e.g. HTMLElement in the browser). These methods are responsible
   for modifying the view state of the rendering engine by adding and removing
   nodes."
  (-insert! [this insert-vnode insert-position]
            "Insert the given virtual node into the current node at the
            specified position")
  (-remove! [this remove-vnode remove-position]
            "Remove the node located at the specified position")
  (child-nodes [this]
               "Get the child node at the given index"))

(defprotocol IContainerNode
  "Allows inspection of child nodes of a virtual or render node"
  (children [this]
            "Get the child nodes of this container node"))

(extend-protocol IVirtualNode
  nil
  (-mount! [_])
  (-unmount! [_ _])
  (-diff [_ _])
  (render [_]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Patches

(defrecord PatchMount [position vnode]
  impl.patch/IPatch
  (-patch! [_ parent-node]
    (-insert! parent-node vnode position)))

(defrecord PatchUnmount [position vnode]
  impl.patch/IPatch
  (-patch! [_ parent-node]
    (-remove! parent-node vnode position)))

(defrecord PatchChild [index patch]
  impl.patch/IPatch
  (-patch! [_ parent-node]
    (impl.patch/-patch! patch (nth (children parent-node) index))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; (Private) Diffing logic

(declare diff-children)

(defn- diff-vnode
  "Diffs two nodes of the same type, delegating to the strategy defined by the
   node type, as well as the children of each node."
  [prev-vnode next-vnode]
  (let [d (-diff prev-vnode next-vnode)]
    (if (satisfies? IContainerNode prev-vnode)
      (apply impl.patch/combine d (diff-children prev-vnode next-vnode))
      d)))

(defn- diff-children
  "For nodes of the same type, determine if any children nodes have changed and
   yield a patch for each modified child."
  [prev-vnode next-vnode]
  (let [prev-children (children prev-vnode)
        next-children (children next-vnode)
        child-count (max (count prev-children) (count next-children))]
    (loop [index 0
           patches (transient [])]
      (if (>= index child-count)
        (not-empty (persistent! patches))
        (recur
          (inc index)
          (let [prev-child (nth prev-children index nil)
                next-child (nth next-children index nil)]
            (if (identical? (type prev-child) (type next-child))
              (if-let [patch (diff-vnode prev-child next-child)]
                (conj! patches (->PatchChild index patch))
                patches)
              (cond-> patches
                prev-child (conj! (->PatchUnmount index prev-child))
                next-child (conj! (->PatchMount index next-child))))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public API

(defn diff-map
  "Diffs two maps, yielding a map that contains only the keys from the union
   of both maps with the values of the second map where the value is not the
   same in the first map. Returns `nil` if the maps are the same."
  [prev-map next-map]
  (when-not (identical? prev-map next-map)
    (let [prev-keys (set (keys prev-map))
          next-keys (set (keys next-map))
          all-keys (cond
                     (= prev-keys next-keys)
                     prev-keys

                     (< (count prev-keys) (count next-keys))
                     (into next-keys prev-keys)

                     :else
                     (into prev-keys next-keys))]
      (loop [res (transient {})
             [k & ks] (seq all-keys)]
        (let [prev-val (get prev-map k)
              next-val (get next-map k)]
          (when-not (= prev-val next-val)
            (assoc! res k next-val))
          (if ks
            (recur res ks)
            (when (pos? (count res))
              (persistent! res))))))))

(defn diff
  "Diffs two virtual render trees rooted at the given node and yields a patch
   that can be applied to the first tree to transform it into the second"
  [prev-vnode next-vnode]
  (if (identical? (type prev-vnode) (type next-vnode))
    (diff-vnode prev-vnode next-vnode)
    ;; TODO
    ))
