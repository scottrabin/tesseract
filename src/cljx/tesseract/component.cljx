(ns tesseract.component)

(defprotocol IShouldUpdate
  (-should-update? [this next-component]))

(defprotocol IWillMount
  (-will-mount [this]))

(defprotocol IDidMount
  (-did-mount [this container]))

(defprotocol IWillUnmount
  (-will-unmount [this]))

(defprotocol IWillUpdate
  "Invoked immediately before rendering when new attrs or state are being
  received. This method is not called for the initial render. Use this as an
  opportunity to perform preparation before an update occurs.
  Should not update state."
  (-will-update [this next-component]))

(defprotocol IDidUpdate
  (-did-update [this prev-component container]))

(defprotocol IComponent
  (-update [this next-component])
  (-render [this]))

(defn should-update? [current-component next-component]
  (and (= (type current-component) (type next-component))
       (-should-update? current-component next-component)))

(defn default-should-update? [this next-component]
  (and
    (= (:attrs this) (:attrs next-component))
    (= (:state this) (:state next-component))))

(defn update [component next-component container]
  (when (should-update? component next-component)
    (when (satisfies? IWillUpdate component)
      (-will-update component next-component))
    (-update component next-component)
    (when (satisfies? IDidUpdate next-component)
      (-did-update next-component component container))))

#+clj
(defn emit-defcomponent
  "Emits forms to define a record and convenience constructor for components"
  [component-name spec-map]
  (when-not (contains? spec-map :render)
    (throw (IllegalArgumentException. "defcomponent requires render to be defined")))
  (let [rec-name (symbol (str component-name "Component"))
        impls [[`IComponent
                `(~'-update [this# next-component#])
                `(~'-render ~@(:render spec-map))]
               [`IShouldUpdate
                (if-let [spec (:should-update? spec-map)]
                  `(~'-should-update? ~@spec)
                  `(~'-should-update? [this# next-component#]
                                      (default-should-update? this# next-component#)))]
               (when-let [spec (:will-update spec-map)]
                 [`IWillUpdate
                  `(~'-will-update ~@spec)])
               (when-let [spec (:did-update spec-map)]
                 [`IDidUpdate
                  `(~'-did-update ~@spec)])
               (when-let [spec (:will-mount spec-map)]
                 [`IWillMount
                  `(~'-will-mount ~@spec)])
               (when-let [spec (:did-mount spec-map)]
                 [`IDidMount
                  `(~'-did-mount ~@spec)])
               ['Object
                `(~'toString [this#] (str (-render this#)))]]]
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
