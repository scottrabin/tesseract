(ns tesseract.component
  (:require [tesseract.dom :as dom]))

(defprotocol IComponent
  (-render [this])
  (-mount! [component root-node cursor])
  (-build! [this prev-component cursor]))

(defprotocol IShouldRender
  "Returns boolean. Invoked before rendering when new attrs or state is
  recieved. This method is not called for the initial render. Use this
  opportunity to return false when you're certain the transition to new attrs
  and state will not require re-render."
  (-should-render? [this next-component]))

(defprotocol IWillMount
  "Returns component, with potentially modified state. Invoked once, immediately
  before initial rendering occurs. Calling set-state! is allowed in this method,
  but will cause an separate render."
  (-will-mount! [this]))

(defprotocol IDidMount
  "Invoked immediately after initial rendering occurs and component has a DOM
  representation in container."
  (-did-mount! [this root-node]))

(defprotocol IWillUnmount
  "Invoked immediately before a component is unmounted from the DOM. Perform
  any necessary cleanup in this method, such as invalidating timers or cleaning
  up any DOM elements that were created with IWillMount."
  (-will-unmount! [this]))

(defprotocol IWillBuild
  "Invoked immediately before rendering when new attrs or state are being
  received. This method is not called for the initial render. Use this as an
  opportunity to perform preparation before an build occurs.
  Should not modify state."
  ;; TODO enforce set-state!/update-state! isn't called within this function
  (-will-build! [this next-component]))

(defprotocol IDidBuild
  "Invoked immediately after building occurs. This method is not called for
  initial render. Use this as an opportunity to operate on the DOM when the
  component has been built."
  (-did-build! [this prev-component root-node]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn render [component] (-render component))

(defn render-str [component] (str (-render component)))

(defn mount! [component root-node cursor]
  (-mount! component root-node cursor))

(defn build! [component prev-component cursor]
  (-build! component prev-component cursor))

(defn will-mount! [component]
  (if (satisfies? IWillMount component)
    (-will-mount! component) ;; TODO probably needs try-catch
    component))

(defn did-mount! [component root-node]
  (when (satisfies? IDidMount component)
    ;; TODO this should be enqueued
    (-did-mount! component root-node)))

(defn will-unmount! [component]
  (when (satisfies? IWillUnmount component)
    (-will-unmount! component)))

(defn will-build! [component next-component]
  (when (satisfies? IWillBuild component)
    (-will-build! component next-component)))

(defn did-build! [component prev-component root-node]
  (when (satisfies? IDidBuild component)
    ;; TODO this should be enqueued
    (-did-build! component prev-component root-node)))

(defn assoc-cursor
  [component cursor]
  (vary-meta component assoc ::cursor cursor))

(defn get-cursor
  [component]
  (::cursor (meta component)))

(defn should-render? [current-component next-component]
  (and (= (type current-component) (type next-component))
       (-should-render? current-component next-component)))

(defn default-should-render? [this next-component]
  (or
    (not= (:attrs this) (:attrs next-component))
    (not= (:state this) (:state next-component))))

(defn- build-child
  "Renders a component, builds the child component, then returns it"
  [component prev-component cursor]
  (let [child (render component)
        child-cursor (conj cursor 0)
        prev-child (-> prev-component :children first)]
    (build! child prev-child child-cursor)))

(defn build-component
  "Returns next-component after rendering it an any of its children"
  [component prev-component cursor]
  (when prev-component
    (will-build! prev-component component))
  (let [child (build-child component prev-component cursor)
        built (-> component
                  (assoc-cursor cursor)
                  (assoc :children [child]))]
    ;; TODO (did-build! built component root-node)
    built))

#+cljs
(defn- mount-child!
  "Renders a component, mounts the child component, then returns it"
  [component cursor]
  (let [child (render component)
        child-cursor (conj cursor 0)]
    (mount! child nil child-cursor)))

#+cljs
(defn mount-component!
  [component root-node cursor]
  (let [component (-> component
                      (assoc-cursor cursor)
                      (will-mount!))
        child (mount-child! component cursor)
        mounted (assoc component :children [child])]
    ; TODO (when (satisfies? IDidMount component) (enqueue-mount-ready! component root-node))
    mounted))

#+clj
(defn emit-defcomponent
  "Emits forms to define a record and convenience constructor for components"
  [component-name spec-map]
  (when-not (contains? spec-map :render)
    (throw (IllegalArgumentException. "defcomponent requires render to be defined")))
  (let [rec-name (symbol (str component-name "Component"))
        impls [[`IComponent
                `(~'-render ~@(:render spec-map))
                `(~'-mount! [this# root-node# cursor#]
                            (mount-component! this# root-node# cursor#))
                `(~'-build! [this# prev# cursor#]
                            (build-component this# prev# cursor#))]
               [`IShouldRender
                (if-let [spec (:should-render? spec-map)]
                  `(~'-should-render? ~@spec)
                  `(~'-should-render? [this# next-component#]
                                      (default-should-render? this# next-component#)))]
               (when-let [spec (:will-build! spec-map)]
                 [`IWillBuild
                  `(~'-will-build! ~@spec)])
               (when-let [spec (:did-build! spec-map)]
                 [`IDidBuild
                  `(~'-did-build! ~@spec)])
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
