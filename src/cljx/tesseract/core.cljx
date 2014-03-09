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

;; TODO This should probably be moved to own namespace or tesseract.component

#+cljs
(defn- mount-children! [nested-children parent-cursor]
  (map-indexed #(c/mount! %2 nil (conj parent-cursor %1))
               (flatten nested-children)))

#+cljs
(defn- build-children! [nested-children prev-children parent-cursor]
  (map-indexed (fn [idx child]
                 (let [child-cursor (conj parent-cursor idx)]
                   (if-let [prev-child (get prev-children idx)]
                     (c/build! child prev-child child-cursor)
                     (c/mount! child nil child-cursor))))
               (flatten nested-children)))

#+cljs
(extend-protocol c/IComponent
  dom/Element
  (-render [this] this)
  (-mount! [this _ cursor]
    (let [children (mount-children! (:children this) cursor)]
      (-> this
          (c/assoc-cursor cursor)
          (c/assoc-children children))))
  (-build! [this prev-component cursor]
    (let [prev-children (:children prev-component)
          children (build-children! (:children this) prev-children cursor)]
      (-> this
          (c/assoc-cursor cursor)
          (c/assoc-children children))))

  string
  (-render [this] this)
  (-mount! [this _ _] this)
  (-build! [this _ cursor] this)

  number
  (-render [this] this)
  (-mount! [this _ _] this)
  (-build! [this _ cursor] this))

#+cljs
(extend-protocol c/IBuiltComponent
  dom/Element
  (-get-children [this] (:children this))
  (-get-child [this k]
    (get-in this [:children k]))
  (-assoc-children [this children]
    (assoc this :children (if (associative? children) children (vec children))))
  (-assoc-child [{children :children :as this} k child]
    (let [children (if (associative? children) children (vec children))]
      (assoc this :children (assoc children k child))))
  (-get-child-in [this path]
    (if (seq path)
      (when-let [child (c/-get-child this (first path))]
        (c/-get-child-in child (rest path)))
      this))
  (-assoc-child-in [{children :children :as this} [k & ks] child]
    (let [children (if (associative? children) children (vec children))]
      (cond
        ks (if-let [next-child (get children k)]
             (->> (c/-assoc-child-in next-child ks child)
                  (assoc children k)
                  (assoc :children this))
             (throw (js/Error. "Failed to associate child at uninitialized path")))

        k (c/-assoc-child this k child)

        :else child))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#+cljs
(defn tick-state! [component next-state-fn cursor]
  (let [[root-id & path] cursor
        container (mount/container-by-root-id mount-env root-id)
        root-component (mount/component-by-root-id mount-env root-id)
        canon-component (c/get-child-in root-component path)
        not-found? (nil? canon-component)
        component (or canon-component component)
        next-component (next-state-fn component)]
    (if (c/should-render? component next-component)
      ;; Rebuild entire thing for now... TODO rebuild next-component, find its respective DOM
      (let [root-component (-> root-component
                               (c/assoc-child-in path next-component)
                               (c/build! root-component [root-id]))]
        (set! (.-innerHTML container) (str root-component))
        (mount/register-component! mount-env root-component root-id)))))

#+cljs
(defn flush-next-state! []
  ;; TODO utilize mount depth (ie cursor length) to update efficiently
  (when-let [[component next-state-fn] (q/dequeue! next-state-queue)]
    (tick-state! component next-state-fn (c/get-cursor component))
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
(defn- replace-component!
  [component new-component id container]
  (tick-state! component (fn [_] new-component) [id]))

#+cljs
(defn mount-into-container!
  [component container]
  (let [id (mount/root-id container)
        existing-component (mount/component-by-root-id mount-env id)]
    (if (and existing-component
             (= (type existing-component) (type component)))
      (replace-component! existing-component component id container)
      (do
        ;; Unmount unmatching component
        (when existing-component
          (unmount-component! existing-component container))
        ;; Mount
        (let [root-component (c/mount! component container [id])]
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
