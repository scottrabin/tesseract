(ns tesseract.core
  #+cljs (:require [tesseract.mount :as mount]
                   [tesseract.component :as c])
  #+clj  (:require [tesseract.component :as c]))

#+cljs
(def ^:private mount-env (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn render [component] (c/-render component))

#+cljs
(defn attach [component container]
  (mount/mount-component! mount-env component container))

#+clj
(defmacro defcomponent [component-name & spec]
  (c/emit-defcomponent
    component-name
    (into {} (for [s spec] [(-> s first keyword) (rest s)]))))
