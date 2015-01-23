(ns tesseract.impl.patch)

(defprotocol IPatch
  (-patch! [_ node] "Apply the patch to the given node"))

(extend-protocol IPatch
  nil
  (-patch! [_ node] node))

(defn- apply-patch!
  "Applies a patch to the node. Essentially just reverses the arguments and
  returns the mutated node to allow a simple `reduce` in `patch!`"
  [node patch]
  (-patch! patch node)
  node)

(defrecord MultiPatch [patches]
  IPatch
  (-patch! [_ node]
    (reduce apply-patch! node patches)))

(defn patch!
  "Apply an ordered collection of patches to the given node"
  [node patches]
  (reduce apply-patch! node patches))

(defn combine
  "Combine a set of patches into one MultiPatch. Returns `nil` if there are no
   meaningful patches given."
  [& patches]
  (let [coll (transient [])]
    (loop [[p & more] patches]
      (when p
        (if (identical? MultiPatch (type p))
          (reduce conj! coll (.-patches p))
          (conj! coll p)))
      (when more
        (recur more)))
    (case (count coll)
      0 nil
      1 (nth coll 0)
      (->MultiPatch (persistent! coll)))))
