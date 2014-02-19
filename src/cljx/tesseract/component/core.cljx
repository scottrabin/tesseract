(ns tesseract.component.core)

(defprotocol IComponent
  (-mount! [component node]
           "Mounts the component into the node")
  (-update! [component next-component]
            "Applies diffs to the component")
  (-render [component]
           "Renders the component to its constituent DOM representation"))