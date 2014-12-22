(ns tesseract.impl.vdom
  "Diff/patch behavior used for browser-based virtual DOM elements"
  (:require
    [tesseract.impl.patch :as impl.patch]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public API

(deftype SetAttributes [attrs]
  impl.patch/IPatch
  (-patch! [_ node]
    ;; TODO
    ))

(declare diff)

(defprotocol IVirtualDOMNode
  (-diff [self other] "Yield a collection of patches that convert the current
                      node into the other"))

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
