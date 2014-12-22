(ns tesseract.impl.vdom
  "Diff/patch behavior for renderable nested elements"
  (:require
    [tesseract.impl.patch :as impl.patch]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public API

(defprotocol IVirtualRenderNode
  (-diff [self other] "Yield a collection of patches that convert the current
                      node into the other")
  (-patch [self patch] "Apply a given patch to the current node")
  (render [self] "Generate the virtual render tree under this node"))

(defn diff
  "Diffs two VirtualDOM trees rooted at the given node and yields a collection
  of patches that can be applied to the first tree to transform it into the second"
  [prev-tree next-tree]
  #_ (prn "prev-tree"
       (satisfies? IVirtualDOMNode prev-tree)
       prev-tree
       "next-tree"
       (satisfies? IVirtualDOMNode next-tree)
       next-tree)
  (-diff prev-tree next-tree))
