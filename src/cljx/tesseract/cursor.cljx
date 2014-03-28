(ns tesseract.cursor)

(def ->cursor vector)

(defn- get-cursor*
  [component]
  (if-let [curs (-> component meta ::cursor)]
    curs
    (throw
     #+clj  (RuntimeException. "Failed to retrieve cursor from component")
     #+cljs (js/Error. "Failed to retrieve cursor from component"))))

(defn assoc-cursor
  [component cursor]
  (vary-meta component assoc ::cursor (atom cursor)))

(defn clear-cursor!
  [component]
  (reset! (get-cursor* component) nil))

(defn get-cursor
  [component]
  @(get-cursor* component))
