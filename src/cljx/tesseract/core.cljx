(ns tesseract.core
  #+cljs (:require [tesseract.mount :as mount]
                   [tesseract.dom :as dom]
                   [tesseract.component :as c]
                   [tesseract.queue :as q])
  #+clj  (:require [tesseract.component :as c]
                   [tesseract.queue :as q]))

#+cljs
(def ^:private mount-env (atom {}))

(def ^:private next-state-queue (atom (q/make-queue)))

(defn- empty-node!
  "http://jsperf.com/emptying-a-node"
  [node]
  (while (.-lastChild node) (.removeChild node (.-lastChild node))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#+cljs
(extend-protocol c/IComponent
  dom/Element
  (-render [this] this)
  (-mount! [this cursor _]
    (let [children (map-indexed #(c/mount! %2 (conj cursor %1) nil)
                                (flatten (:children this)))]
      (-> this
          (c/assoc-cursor cursor)
          (assoc :children (vec children)))))
  (-build! [this prev-component cursor]
    (let [prev-children (:children prev-component)
          children (map-indexed (fn [idx child]
                                  (let [child-cursor (conj cursor idx)]
                                    (if-let [prev-child (get prev-children idx)]
                                      (c/build! child prev-child child-cursor)
                                      (c/mount! child child-cursor nil))))
                                (flatten (:children this)))]
      (-> this
          (c/assoc-cursor cursor)
          (assoc :children (vec children)))))

  string
  (-render [this] this)
  (-mount! [this _ _] this)
  (-build! [this _ cursor] this)

  number
  (-render [this] this)
  (-mount! [this _ _] this)
  (-build! [this _ cursor] this))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-component
  "Returns component at path or nil if not found. Path is a sequence of indicies
  in nested children collections."
  [root-component path]
  (if (seq path)
    (get-in (:children root-component) (interpose :children path))
    root-component))

(defn assoc-component
  [root-component path component]
  (if (seq path)
    (assoc-in root-component
              (cons :children (interpose :children path))
              component)
    component))

#+cljs
(defn tick-state! [component next-state-fn]
  (let [cursor (c/get-cursor component)
        [root-id & path] cursor
        container (mount/container-by-root-id mount-env root-id)
        root-component (mount/component-by-root-id mount-env root-id)
        canon-component (get-component root-component path)
        not-found? (nil? canon-component)
        component (or canon-component component)
        next-component (next-state-fn component)]
    (if (c/should-render? component next-component)
      ;; Rebuild entire thing for now... TODO rebuild next-component, find its respective DOM
      (let [root-component (-> root-component
                               (assoc-component path next-component)
                               (c/build! root-component [root-id]))]
        (set! (.-innerHTML container) (str root-component))
        (mount/register-component! mount-env root-component root-id)))))

#+cljs
(defn flush-next-state! []
  ;; TODO utilize mount depth (ie cursor length) to update efficiently
  (when-let [[component next-state-fn] (q/dequeue! next-state-queue)]
    (tick-state! component next-state-fn)
    (recur)))

#+cljs
(defn set-state!
  [component state]
  (q/enqueue! next-state-queue
              [component #(assoc % :state state)]))

#+cljs
(defn update-state!
  [component f & args]
  (q/enqueue! next-state-queue
              [component #(assoc % :state (apply f (:state %) args))]))

#+cljs
(defn unmount-component!
  ([container]
   (if-let [component (mount/component-by-root-id
                        mount-env
                        (mount/root-id container))]
     (unmount-component! component container)))
  ([component container]
   (when-let [id (mount/root-id container)]
     (c/will-unmount! component)
     (mount/unregister-root-id! mount-env id)
     (empty-node! container)
     true)))

#+cljs
(defn unmount-all!
  "Unmounts all currently mounted components. Useful for tests"
  []
  (doseq [id (mount/root-ids mount-env)]
    (let [component (mount/component-by-root-id mount-env id)
          container (mount/container-by-root-id mount-env id)]
      (unmount-component! component container))))

#+cljs
(defn mount-into-container!
  [component container]
  (let [id (mount/root-id container)
        existing-component (mount/component-by-root-id mount-env id)]
    (if (and existing-component
             (= (type existing-component) (type component)))
      (throw (js/Error. "Replacing component not implemented"))
      ;(c/update existing-component component container)
      (do
        (when existing-component
          (unmount-component! existing-component container))

        (let [root-component (c/mount! component [id] container)]
          (mount/register-component! mount-env root-component id)
          (mount/register-container! mount-env container id)

          ;; TODO actual diffs
          (set! (.-innerHTML container) (str root-component))
          ;; TODO trigger enqueued IDidMount
          )))))

#+cljs
(defn attach! [component container]
  (mount-into-container! component container)
  (flush-next-state!))

#+clj
(defmacro defcomponent [component-name & spec]
  (c/emit-defcomponent
    component-name
    (into {} (for [s spec] [(-> s first keyword) (rest s)]))))
