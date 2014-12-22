(ns tesseract.impl.component
  (:require
    [tesseract.impl.vdom :as vdom]))

#+clj
(def ^:private default-spec
  {:name
   {:validate
    (fn [c]
      (cond
        (nil? c)
        "Component name not defined"))
    :transform
    (fn [c]
      (str (first c)))}

   :initial-state
   {:validate
    (fn [body]
      (when-not (nil? body)
        (cond
          (not= 1 (count body))
          "must have exactly one form")))
    :transform
    (fn [body]
      (first body))}

   :render
   {:validate
    (fn [[binding-form & [body & deny] :as form]]
      (cond
        (nil? form)
        "`render` method must be defined"

        (not (vector? binding-form))
        "must have a binding-form as a first argument"

        (not= 3 (count binding-form))
        (format "binding-form must take 3 arguments, not %d"
                (count binding-form))

        (nil? body)
        "must specify a render body"

        (not (empty? deny))
        "too many forms in `render` body"))
    :transform
    (fn [[[binding-attrs binding-state binding-child] body]]
      `(fn [~binding-attrs ~binding-state ~binding-child]
         ~body))}})

#+clj
(defn- convert-component-spec
  "Converts a component spec from the raw macro input:

  (defcomponent Foo
    (initial-state {:bar? false})
    (render [attrs state children] ...))

  into a specification map that combines the default value for all spec options
  with the overridden values

  {:initial-state {:bar? false}
   :render '([attrs state children]) ...}"
  [user-spec]
  (let [user-spec-map (into {} (for [[k & v] user-spec] [(keyword k) v]))]
    (into {}
          (for [[kw {:keys [validate transform]}] default-spec
                :let [value (get user-spec-map kw)]]
            (if-let [error-msg (validate value)]
              (throw (IllegalArgumentException.
                       (format "Invalid specification for `%s`: %s (got: %s)"
                               (name kw) error-msg value)))
              [kw (transform value)])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public API

(defrecord VirtualDOMNode [attrs state children]
  #+clj
  clojure.lang.Named
  #+clj
  (getName [this]
    (get-in (meta this) [::config :name]))

  #+cljs
  cljs.core/INamed
  #+cljs
  (-name [this]
    (get-in (meta this) [::config :name]))

  Object
  (toString [this]
    (str (vdom/render this)))

  #+clj clojure.lang.IFn
  ;; TODO throw error? just use bare component
  #+clj (invoke [this] this)
  #+clj (invoke [this attrs]
          (assoc this :attrs attrs))
  #+clj (invoke [this attrs child]
          (assoc this
                 :attrs attrs
                 :children [child]))
  #+clj (invoke [this attrs c1 c2]
          (assoc this
                 :attrs attrs
                 :children [c1 c2]))
  #+clj (invoke [this attrs c1 c2 c3]
          (assoc this
                 :attrs attrs
                 :children [c1 c2 c3]))
  #+clj (applyTo [this args]
          (clojure.lang.AFn/applyToHelper this args))

  ;; TODO why does cljs define `-invoke` and clj `invoke`?
  #+cljs cljs.core/IFn
  #+cljs (-invoke [this] this)
  #+cljs (-invoke [this attrs]
           (assoc this :attrs attrs))
  #+cljs (-invoke [this attrs c1]
           (assoc this
                  :attrs attrs
                  :children [c1]))
  #+cljs (-invoke [this attrs c1 c2]
           (assoc this
                  :attrs attrs
                  :children [c1 c2]))
  #+cljs (-invoke [this attrs c1 c2 c3]
           (assoc this
                  :attrs attrs
                  :children [c1 c2 c3]))

  vdom/IVirtualRenderNode
  (-diff [_ other])
  (-patch [_ patch])
  (render [this]
    (let [render-fn (get-in (meta this) [::config :render])]
      (render-fn attrs state children))))

#+clj
(defn emit-defcomponent
  "Accepts the component name & spec and converts it into a proper definition
  for a component."
  [component-name spec]
  (try
    (let [component-spec (-> spec
                             (conj (list :name component-name))
                             convert-component-spec)
          initial-state (:initial-state component-spec)]
      `(def ~component-name (with-meta
                              (->VirtualDOMNode {} ~initial-state [])
                              {::config ~(dissoc component-spec :initial-state)})))
    (catch Exception e
      (throw (IllegalArgumentException.
                 (format "Error in component specification for %s: %s"
                         (str component-name)
                         (.. e getMessage))
                 e)))))
