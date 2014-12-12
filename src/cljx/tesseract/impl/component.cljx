(ns tesseract.impl.component)

#+clj
(def ^:private default-spec
  {:initial-state {:validate (fn [body]
                               (when-not (nil? body)
                                 (cond
                                   (not= 1 (count body))
                                   "must have exactly one form")))}
   :render {:validate (fn [[binding-form & [body & deny] :as form]]
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
                          "too many forms in `render` body"))}})

#+clj
(defn- ^boolean cljs?
  []
  (not (nil? (find-ns 'cljs.core))))

#+clj
(defn- component-names
  "Converts a component name symbol into a vector of
   [<Component record name>
    <Component helper function name>
    <Component fully-qualified symbol>]

  (component-names 'FooBar) ;; => ['FooBarComponent 'FooBar]"
  [component-name]
  [(symbol (str component-name "Component"))
   component-name
   (symbol (name (ns-name *ns*)) (name component-name))])

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
          (for [[kw {:keys [validate]}] default-spec
                :let [value (get user-spec-map kw)]]
            (if-let [error-msg (validate value)]
              (throw (IllegalArgumentException.
                       (format "Invalid specification for `%s`: %s (got: %s)"
                               (name kw) error-msg value)))
              [kw value])))))

#+clj
(defn- emit-component-record
  "Converts a component specification (the output of convert-component-spec)
   into a component record definition"
  [component-name component-spec]
  ;; Define a new record type for the component to store the current
  ;; attrs, state, and children
  (let [[component-defname component-name] (component-names component-name)
        [name-iface name-fn] (if (cljs?)
                               ['cljs.core/INamed '-name]
                               ['clojure.lang.Named 'getName])]
    `(defrecord ~component-defname [~'attrs ~'state ~'children]
       ~name-iface
       (~name-fn [~'_] ~(str component-name))

       ~'Object
       (~'toString [self#]
         (str (render self#)))

       IComponent
       (~'render [~'_]
         (let [~(first (:render component-spec)) [~'attrs ~'state ~'children]]
           ~(second (:render component-spec)))))))

#+clj
(defn- emit-component-helper-fn
  "Converts a component specification (the output of convert-component-spec)
  into a component helper function definition"
  [component-name component-spec]
  (let [[component-defname component-helper-fn _] (component-names component-name)
        ctor (symbol (str "->" component-defname))]
    `(let [initial-state# ~(first (:initial-state component-spec))
           bare-component# (~ctor nil initial-state# nil)]
       ;; Define the user-supplied symbol for the component to be a
       ;; convenience function that uses the same nil-attr'd component
       ;; for empty invocations
       ;; TODO can we avoid the function call entirely?
       (defn ~component-helper-fn
         ([]
          bare-component#)
         ([attrs#]
          (~ctor attrs# initial-state# nil))
         ([attrs# & children#]
          (~ctor attrs# initial-state# children#))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public API

(defprotocol IComponent
  (render [c] "Returns the DOM tree rooted at this node"))

#+clj
(defn emit-defcomponent
  "Accepts the component name & spec and converts it into a proper definition
  for a component."
  [component-name spec]
  (assert (nil? (re-matches #"(?i)component" (str component-name)))
          "\"component\" in component name is implied")
  (try
    (let [component-spec (convert-component-spec spec)]
      `(do
         ~(emit-component-record component-name component-spec)
         ~(emit-component-helper-fn component-name component-spec)))
    (catch Exception e
      (let [[_ _ component-fqn] (component-names component-name)]
        (throw (IllegalArgumentException.
                 (format "Error in component specification for %s: %s"
                         (str component-fqn)
                         (.. e getMessage))))))))
