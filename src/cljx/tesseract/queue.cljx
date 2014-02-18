(ns tesseract.queue)

(defn make-queue []
  #+cljs #queue []
  #+clj (clojure.lang.PersistentQueue/EMPTY))

#+clj
(defn dequeue! [queue]
  (loop []
    (let [q @queue
          head (peek q)
          tail (pop q)]
      (if (compare-and-set! queue q tail)
        head
        (recur)))))

#+cljs
(defn dequeue! [queue]
  (let [q @queue
        head (peek q)
        tail (pop q)]
    (reset! queue tail) ; JS is single-threaded, compare-and-set! not needed
    head))

(defn enqueue! [queue v] (swap! queue conj v))
