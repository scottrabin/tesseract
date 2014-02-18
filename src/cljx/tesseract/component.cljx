(ns tesseract.component
  #+clj (:require [tesseract.dom :as dom]
                  [clojure.set])
  #+cljs (:require [tesseract.dom :as dom]
                   [tesseract.mount :as mount]
                   [clojure.set]))

(defprotocol IShouldUpdate ;; TODO rename IShouldRender
  (-should-update? [this next-component]))

(defprotocol IWillMount
  (-will-mount! [this]))

(defprotocol IDidMount
  (-did-mount! [this container]))

(defprotocol IWillUnmount
  (-will-unmount! [this]))

(defprotocol IWillUpdate
  "Invoked immediately before rendering when new attrs or state are being
  received. This method is not called for the initial render. Use this as an
  opportunity to perform preparation before an update occurs.
  Should not update state."
  (-will-update [this next-component]))

(defprotocol IDidUpdate
  (-did-update [this prev-component container]))

(defprotocol IComponent
  (-build [this cursor])
  (-render [this]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn render [component] (-render component))

(defn render-str [component] (str (-render component)))

(defn build [component cursor] (-build component cursor))

(defn assoc-cursor
  [component cursor]
  (vary-meta component assoc ::cursor cursor))

(defn get-cursor
  [component]
  (::cursor (meta component)))

(defn should-update? [current-component next-component]
  (and (= (type current-component) (type next-component))
       (-should-update? current-component next-component)))

(defn default-should-update? [this next-component]
  (or
    (not= (:attrs this) (:attrs next-component))
    (not= (:state this) (:state next-component))))

(defn- map-key-diff
  "Returns tuple of [common-keys added-keys removed-keys]"
  [m1 m2]
  (let [ks1 (set (keys m1))
        ks2 (set (keys m2))]
    [(clojure.set/intersection ks1 ks2)
     (clojure.set/difference ks2 ks1)
     (clojure.set/difference ks1 ks2)]))

(defn style-diff [prev-styles next-styles]
  (when (not= prev-styles next-styles)
    (let [[=ks +ks -ks] (map-key-diff prev-styles next-styles)]
      (concat
        (for [style +ks] [:set-style style (get next-styles style)])
        (for [style -ks] [:remove-style style])
        (reduce (fn [ops style]
                  (let [next (get next-styles style)]
                    (if (not= (get prev-styles style) next)
                      (conj ops [:set-style style next])
                      ops)))
                nil
                =ks)))))

(defn attrs-diff [prev-attrs next-attrs]
  (let [[=ks +ks -ks] (map-key-diff prev-attrs next-attrs)]
    (concat
      (for [attr +ks] [:set-attr attr (get next-attrs attr)])
      (for [attr -ks] [:remove-attr attr])
      (reduce (fn [ops attr]
                (let [prev (get prev-attrs attr)
                      next (get next-attrs attr)]
                  (if (= attr :style)
                    (concat ops (style-diff (:style prev-attrs)
                                            (:style next-attrs)))
                    (if (= (dom/to-attr prev) (dom/to-attr next))
                      ops
                      (conj ops [:set-attr attr (get next-attrs attr)])))))
              nil
              =ks))))

(defn build-component
  "Returns next-component after rendering it an any of its children"
  [component cursor]
  (let [child (build (render component) (conj cursor 0))]
    (-> component
        (assoc-cursor cursor)
        (assoc :children [child]))))

(defn will-mount! [component]
  (if (satisfies? IWillMount component)
    (-will-mount! component)
    component))

#+cljs
(defn mount-component!
  [component cursor]
  (let [component (-> component
                      (assoc-cursor cursor)
                      (will-mount!))
        child (mount/-mount! (render component) (conj cursor 0))
        mounted (assoc component :children [child])]
    ; TODO (when (satisfies? IDidMount component) (enqueue-mount-ready! component))
    mounted))

#+clj
(defn emit-defcomponent
  "Emits forms to define a record and convenience constructor for components"
  [component-name spec-map]
  (when-not (contains? spec-map :render)
    (throw (IllegalArgumentException. "defcomponent requires render to be defined")))
  (let [rec-name (symbol (str component-name "Component"))
        impls [[`IComponent
                `(~'-build [this# cursor#] (build-component this# cursor#))
                `(~'-render ~@(:render spec-map))]
               [`IShouldUpdate
                (if-let [spec (:should-update? spec-map)]
                  `(~'-should-update? ~@spec)
                  `(~'-should-update? [this# next-component#]
                                      (default-should-update? this# next-component#)))]
               ['tesseract.mount/IMount
                `(~'-mount! [this# cursor#] (mount-component! this# cursor#))]
               (when-let [spec (:will-update spec-map)]
                 [`IWillUpdate
                  `(~'-will-update ~@spec)])
               (when-let [spec (:did-update spec-map)]
                 [`IDidUpdate
                  `(~'-did-update ~@spec)])
               (when-let [spec (:will-mount! spec-map)]
                 [`IWillMount
                  `(~'-will-mount! ~@spec)])
               (when-let [spec (:did-mount! spec-map)]
                 [`IDidMount
                  `(~'-did-mount! ~@spec)])
               (when-let [spec (:will-unmount spec-map)]
                 [`IWillUnmount
                  `(~'-will-unmount! ~@spec)])
               ['Object
                `(~'toString [this#] (render-str this#))]]]
    `(do
       (defrecord ~rec-name [~'attrs ~'children ~'state]
         ~@(apply concat impls))
       (defn ~component-name
         [attrs# & children#]
         (let [state# ~(:default-state spec-map '{})]
           (new ~rec-name
                attrs#
                (vec children#)
                state#))))))
