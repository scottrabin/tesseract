(ns tesseract.core
  #+cljs (:require [tesseract.mount :as mount]
                   [tesseract.dom :as dom]
                   [tesseract.env :as env]
                   [tesseract.attrs]
                   [tesseract.cursor]
                   [tesseract.component :as c]
                   [tesseract.queue :as q]
                   [tesseract.events])
  #+clj  (:require [tesseract.dom :as dom]
                   [tesseract.attrs]
                   [tesseract.cursor]
                   [tesseract.component :as c]
                   [tesseract.queue :as q]))

(def ^:private tesseract-env (env/create-env))

(def ^:private next-state-queue (atom (q/make-queue)))

(defn- empty-node!
  "http://jsperf.com/emptying-a-node"
  [node]
  (while (.-lastChild node) (.removeChild node (.-lastChild node))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO This should probably be moved to own namespace or tesseract.component

(defn- mount-children! [nested-children parent-cursor]
  (map-indexed #(c/mount! %2 nil (conj parent-cursor %1))
               (flatten nested-children)))

(defn- build-children! [nested-children prev-children parent-cursor]
  (map-indexed (fn [idx child]
                 (let [child-cursor (conj parent-cursor idx)]
                   (if-let [prev-child (get prev-children idx)]
                     (c/build! child prev-child child-cursor)
                     (c/mount! child nil child-cursor))))
               (flatten nested-children)))

(extend-type #+clj tesseract.dom.Element #+cljs dom/Element
  c/IComponent
  (-render [this] this)
  (-mount! [this _ cursor]
    (let [children (mount-children! (:children this) cursor)]
      (-> this
          (tesseract.cursor/assoc-cursor cursor)
          (tesseract.attrs/build-attrs! nil tesseract-env)
          (c/assoc-children children))))
  (-build! [this prev-component cursor]
    (let [prev-children (:children prev-component)
          children (build-children! (:children this) prev-children cursor)]
      (-> this
          (tesseract.cursor/assoc-cursor cursor)
          (tesseract.attrs/build-attrs! prev-component tesseract-env)
          (c/assoc-children children))))

  c/IBuiltComponent
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
             (throw
               #+clj  (RuntimeException. "Failed to associate child at uninitialized path")
               #+cljs (js/Error. "Failed to associate child at uninitialized path")))

        k (c/-assoc-child this k child)

        :else child))))

(extend-protocol c/IComponent
  #+clj String #+cljs string
  (-render [this] this)
  (-mount! [this _ _] this)
  (-build! [this _ cursor] this)

  #+clj java.lang.Number #+cljs number
  (-render [this] this)
  (-mount! [this _ _] this)
  (-build! [this _ cursor] this))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#+cljs
(defn tick-state! [component next-state-fn cursor]
  (let [[root-id & path] cursor
        container (mount/container-by-root-id tesseract-env root-id)
        root-component (mount/component-by-root-id tesseract-env root-id)
        canon-component (c/get-child-in root-component path)
        not-found? (nil? canon-component)
        component (or canon-component component)
        next-component (next-state-fn component)]
    (if (c/should-render? component next-component)
      ;; Rebuild entire thing for now... TODO rebuild next-component, find its respective DOM
      (let [root-cursor (tesseract.cursor/->cursor root-id)
            root-component (-> root-component
                               (c/assoc-child-in path next-component)
                               (c/build! root-component root-cursor))]
        (set! (.-innerHTML container) (str root-component))
        (env/register-component! tesseract-env root-component root-id)))))

#+cljs
(defn flush-next-state! []
  ;; TODO utilize mount depth (ie cursor length) to update efficiently
  (when-let [[component next-state-fn] (q/dequeue! next-state-queue)]
    (tick-state! component next-state-fn (tesseract.cursor/get-cursor component))
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
                        tesseract-env
                        (mount/root-id container))]
     (unmount-component! component container)))
  ([component container]
   (when-let [id (mount/root-id container)]
     (c/will-unmount! component)
     (env/unregister-root-id! tesseract-env id)
     (empty-node! container)
     true)))

#+cljs
(defn unmount-all!
  "Unmounts all currently mounted components. Useful for tests"
  []
  (doseq [id (mount/root-ids tesseract-env)]
    (let [component (mount/component-by-root-id tesseract-env id)
          container (mount/container-by-root-id tesseract-env id)]
      (unmount-component! component container))))
#+cljs
(defn- replace-component!
  [component new-component id container]
  (tick-state! component (fn [_] new-component) [id]))

#+cljs
(defn mount-into-container!
  [component container]
  (let [id (mount/root-id container)
        existing-component (mount/component-by-root-id tesseract-env id)]
    (if (and existing-component
             (= (type existing-component) (type component)))
      (replace-component! existing-component component id container)
      (do
        ;; Unmount unmatching component
        (when existing-component
          (unmount-component! existing-component container))
        ;; Mount
        (let [root-component (c/mount! component container [id])]
          (env/register-component! tesseract-env root-component id)
          (env/register-container! tesseract-env container id)

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
