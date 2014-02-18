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
  (-build [this cursor]
    (let [children (map-indexed #(c/build %2 (conj cursor %1))
                                (flatten (:children this)))]
      (-> this
          (c/assoc-cursor cursor)
          (assoc :children (vec children)))))

  string
  (-render [this] this)
  (-build [this cursor] this)

  number
  (-render [this] this)
  (-build [this cursor] this))

#+cljs
(extend-protocol mount/IMount
  dom/Element
  (-mount! [this cursor]
    (let [children (map-indexed #(mount/-mount! %2 (conj cursor %1))
                                (flatten (:children this)))]
      (-> this
          (c/assoc-cursor cursor)
          (assoc :children (vec children)))))
  string
  (-mount! [this cursor] this)

  number
  (-mount! [this cursor] this))

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
                               (c/build [root-id]))]
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
  (q/enqueue! next-state-queue [component #(assoc % :state state)]))

#+cljs
(defn update-state!
  [component f & args]
  (q/enqueue! next-state-queue [component #(apply update-in % [:state] f args)]))

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
(defn mount-component! [component cursor] (mount/-mount! component cursor))

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

        (let [root-component (mount-component! component [id])]
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
