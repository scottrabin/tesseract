(ns tesseract.component
  (:require [tesseract.dom :as dom]
            [tesseract.cursor]))

(defprotocol IComponent
  (-render [this])
  (-mount! [component root-node cursor])
  (-build! [this prev-component cursor]))

(defprotocol IBuiltComponent
  (-get-children [this])
  (-get-child [this k])
  (-assoc-children [this children])
  (-assoc-child [this k child])
  (-get-child-in [this path])
  (-assoc-child-in [this path child]))

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

(defn get-children [component]
  (-get-children component))

(defn get-child [component k]
  (-get-child component k))

(defn assoc-child [component k child]
   (-assoc-child component k child))

(defn get-child-in [component path]
  (-get-child-in component path))

(defn assoc-children [component children]
  (-assoc-children component children))

(defn assoc-child-in [component path child]
  (-assoc-child-in component path child))

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

(defn build-component!
  "Returns next-component after rendering it an any of its children"
  [component prev-component cursor]
  (when prev-component
    (will-build! prev-component component))
  (let [child (build-child component prev-component cursor)
        built (-> component
                  (tesseract.cursor/assoc-cursor cursor)
                  (assoc-children [child]))]
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
                      (tesseract.cursor/assoc-cursor cursor)
                      (will-mount!))
        child (mount-child! component cursor)
        mounted (assoc-children component [child])]
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
                            (build-component! this# prev# cursor#))]
               [`IBuiltComponent
                `(~'-get-children [this#] (::children this#))
                `(~'-get-child [this# k#] (get (::children this#) k#))
                `(~'-assoc-children [this# children#]
                                    (assoc this# ::children (if (associative? children#)
                                                              children#
                                                              (vec children#))))
                `(~'-assoc-child [this# k# child#]
                                 (let [children# (::children this#)
                                       children# (if (associative? children#) children# (vec children#))]
                                   (assoc this# ::children (assoc children# k# child#))))
                `(~'-get-child-in [this# path#]
                                  (if (seq path#)
                                    (when-let [child# (-get-child this# (first path#))]
                                      (-get-child-in child# (rest path#)))
                                    this#))
                `(~'-assoc-child-in [this# [k# & ks#] child#]
                                    (let [children# (::children this#)
                                          children# (if (associative? children#) children# (vec children#))]
                                      (cond
                                        ks# (if-let [next-child# (get children# k#)]
                                              (->> (-assoc-child-in next-child# ks# child)
                                                   (assoc children# k#)
                                                   (assoc this# ::children))
                                              (throw (js/Error. "Failed to associate child at uninitialized path")))

                                        k# (-assoc-child this# k# child#)

                                        :else child#)))]
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
         (new ~rec-name
              attrs#
              (vec children#)
              ~(if-let [default-state (:default-state spec-map)]
                 ~@default-state
                 '{}))))))
