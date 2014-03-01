(ns tesseract.attrs
  (:require [tesseract.dom :as dom]
            [clojure.set]))

(def event-names
  #{:blur
    :click
    :context-menu
    :copy
    :cut
    :double-click
    :drag
    :drag-end
    :drag-enter
    :drag-exit
    :drag-leave
    :drag-over
    :drag-start
    :drop
    :focus
    :input
    :key-down
    :key-press
    :key-up
    :load
    :error
    :mouse-down
    :mouse-move
    :mouse-out
    :mouse-over
    :mouse-up
    :paste
    :reset
    :scroll
    :submit
    :touch-cancel
    :touch-end
    :touch-move
    :touch-start
    :wheel})

(defn- map-key-diff
  "Returns tuple of [common-keys added-keys removed-keys]"
  [m1 m2]
  (let [ks1 (set (keys m1))
        ks2 (set (keys m2))]
    [(clojure.set/intersection ks1 ks2)
     (clojure.set/difference ks2 ks1)
     (clojure.set/difference ks1 ks2)]))

(defn style-diff [prev-styles next-styles]
  (when (not= prev-styles next-styles)
    (let [[=ks +ks -ks] (map-key-diff prev-styles next-styles)]
      (concat
        (for [style +ks] [:set-style style (get next-styles style)])
        (for [style -ks] [:remove-style style])
        (reduce (fn [ops style]
                  (let [next (get next-styles style)]
                    (if (not= (get prev-styles style) next)
                      (conj ops [:set-style style next])
                      ops)))
                nil
                =ks)))))

(defn attrs-diff [prev-attrs next-attrs]
  (let [[=ks +ks -ks] (map-key-diff prev-attrs next-attrs)]
    (concat
      (for [attr +ks] [:set-attr attr (get next-attrs attr)])
      (for [attr -ks] [:remove-attr attr])
      (reduce (fn [ops attr]
                (let [prev (get prev-attrs attr)
                      next (get next-attrs attr)]
                  (if (= attr :style)
                    (concat ops (style-diff (:style prev-attrs)
                                            (:style next-attrs)))
                    (if (= (dom/to-attr prev) (dom/to-attr next))
                      ops
                      (conj ops [:set-attr attr (get next-attrs attr)])))))
              nil
              =ks))))

(defmulti build-attr (fn [attrs component attr value] (keyword attr)) :default ::default)

(defmethod build-attr ::default
  [attrs component attr value]
  (assoc attrs attr (dom/to-attr value)))

;; on-* event attrs don't affect DOM attrs
(doseq [event-name event-names]
  (defmethod build-attr (keyword (str "on-" (name event-name)))
    [attrs component attr value]
    ;; TODO BIND
    attrs))

(defn build-attrs [component]
  (reduce (fn [attrs [attr value]] (build-attr attrs component attr value))
          {}
          (:attrs component)))
