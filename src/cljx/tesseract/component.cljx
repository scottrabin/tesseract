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
  (-did-update [this prev-component root-node]))

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
  [component-name spec-map]
  (let [rec-name (symbol (str component-name "Component"))]
    `(do
       (defrecord ~rec-name [~'attrs ~'children ~'state ~'bound-methods]
         IShouldUpdate
         ~(if (contains? spec-map :should-update?)
            `(~'-should-update? ~@(:should-update? spec-map))
            `(~'-should-update? [this# next-component#]
                               (default-should-update? this# next-component#)))
         IComponent
         (~'-update [this# next-component#]
           )
         (~'-render ~@(:render spec-map)))
       ~@(for [[protocol method-key] {`IWillUpdate :will-update
                                      `IDidUpdate :did-update
                                      `IWillMount :will-mount
                                      `IDidMount :did-mount}
               :when (contains? spec-map method-key)]
           `(extend-type ~rec-name
              ~protocol
              (~(symbol (str "-" (name method-key))) ~(spec-map method-key))))
       (defn ~component-name
         [attrs# & children#]
         (let [rec# (new ~rec-name
                         attrs#
                         (vec children#)
                         ~(spec-map :default-state {})
                         nil)]
           (assoc rec# :bound-methods
                  (into {} (map (fn [[k# method#]]
                                  [k# (partial method# rec#)])
                                ~(:bound-methods spec-map)))))))))
