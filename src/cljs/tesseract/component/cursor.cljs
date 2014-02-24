(ns tesseract.component.cursor)

(defn assoc-cursor
  "Return a component with the given cursor"
  [component cursor]
  (vary-meta component assoc ::cursor))

(defn get-cursor
  "Get the cursor for a given component"
  [component cursor]
  (-> component meta ::cursor))
