(ns tesseract.dom.core
  "DOM manipulation functions")

(defn empty!
  "Empty a node of all contents
  See http://jsperf.com/emptying-a-node for implementation perf results"
  [node]
  (while (.-lastChild node)
    (.removeChild node (.-lastChild node))))

(defn attr
  "Get the value of an attribute on a given node"
  [node k]
  (.getAttribute node k))

(defn set-attr!
  "Set the value of an attribute on a node"
  [node k v]
  (.setAttribute node k (str v)))
