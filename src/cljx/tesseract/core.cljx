(ns tesseract.core
  #+cljs (:require [tesseract.mount :as mount]))

#+cljs
(def ^:private mount-env (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol IInitState
  (init-state [this]))

(defprotocol IShouldUpdate
  (should-update? [this next-component]))

(defprotocol IWillMount
  (will-mount [this]))

(defprotocol IDidMount
  (did-mount [this node]))

(defprotocol IWillUnmount
  (will-unmount [this]))

(defprotocol IWillUpdate
  "Invoked immediately before rendering when new attrs or state are being
  received. This method is not called for the initial render. Use this as an
  opportunity to perform preparation before an update occurs.
  Should not update state."
  (will-update [this next-component]))

(defprotocol IDidUpdate
  (did-update [this prev-component root-node]))

(defprotocol IComponent
  (-update [this])
  (-render [this]))

(defn render [component]
  (-render component))

(defn update [component next-component]
  (when (should-update? component next-component)
    (when (satisfies? IWillUpdate component)
      (will-update component next-component))

    ;; TODO ME

    ;(when (satisfies? IDidUpdate component)
      ;(did-update next-component component))
    ))

#+cljs
(defn attach [component container]
  (mount/mount-component! mount-env component container))

#+clj
(defn parse-spec [spec]
  (into {} (for [s spec] [(-> s first keyword) (rest s)])))

#+clj
(defmacro defcomponent [name & spec]
  (let [spec-map (parse-spec spec)
        rec-name (symbol (str name "Component"))]
    `(do
       (defrecord ~rec-name [~'attrs ~'children ~'state ~'bound-methods]
         IComponent
         (~'-update [this#]
           )
         (~'-render ~@(:render spec-map))

         tesseract.mount/IMount
         (~'mount! [this# id# container#]
           (set! (.-innerHTML container#)
                 (str (render this#))))
         )
       ~@(for [[protocol method-key] {`IShouldUpdate :should-update?
                                      `IWillUpdate :will-update
                                      `IDidUpdate :did-update
                                      `IWillMount :will-mount
                                      `IDidMount :did-mount}
               :when (contains? spec-map method-key)]
           `(extend-type ~rec-name
              ~protocol
              (~(symbol (name method-key)) ~(spec-map method-key))))
       (defn ~name
         [attrs# & children#]
         (let [rec# (new ~rec-name
                         attrs#
                         (vec children#)
                         ~(spec-map :default-state {})
                         nil)]
           (assoc rec# :bound-methods
                  (into {} (map (fn [[name# method#]]
                                  [name# (partial method# rec#)])
                                ~(:bound-methods spec-map)))))))))

