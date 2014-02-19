(ns tesseract.core
  #+cljs (:require [tesseract.mount :as mount]
                   [tesseract.component.core]
                   [tesseract.component :as component])
  #+clj  (:require [tesseract.component :as component]))

#+cljs
(def ^:private mount-env (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn render [component] (tesseract.component.core/-render component))

#+cljs
(defn attach [component container]
  (mount/mount-component! mount-env component container))

#+clj
(defmacro defcomponent [component-name & spec]
  (component/emit-defcomponent
    component-name
    (into {} (for [s spec] [(-> s first keyword) (rest s)]))))
