(ns tesseract.component
  (:require [tesseract.component.core :refer [IComponent]]))

#+clj
(defn emit-defcomponent
  "Emits forms to define a record and convenience constructor for components"
  [component-name spec-map]
  (when-not (contains? spec-map :render)
    (throw (IllegalArgumentException. "defcomponent requires render to be defined")))
  (let [cmpt-name (symbol (str component-name "Component"))
        default-state (if (contains? spec-map :default-state)
                        ; if the user defined a default state, get it from the seq
                        (-> spec-map :default-state first)
                        ; otherwise, default to empty
                        {})]
    `(do
       ; Emit the component record type
       (defrecord ~cmpt-name [~'attrs ~'children ~'state]
         IComponent
         (~'-mount! [component# container#]
                    ; TODO
                    )
         (~'-update! [component# next-component#]
                     ;TODO
                     )
         (~'-render ~@(:render spec-map))
         ~'Object
         (~'toString [this#] (str (tesseract.component.core/-render this#))))
       ; Emit the helper function to generate elements of the above type
       (defn ~component-name
         [attrs# & children#]
         (new ~cmpt-name
              attrs#
              (vec children#)
              ~default-state)))))
