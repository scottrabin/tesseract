(ns tesseract.impl.patch)

(defprotocol IPatch
  (-patch! [_ node] "Apply the patch to the given node"))

(defn- apply-patch
  "Applies a patch to the node. Essentially just reverses the arguments and
  returns the mutated node to allow a simple `reduce` in `patch!`"
  [node patch]
  (-patch! patch node)
  node)

(defn patch!
  "Apply an ordered collection of patches to the given node"
  [node patches]
  (reduce apply-patch node patches))
