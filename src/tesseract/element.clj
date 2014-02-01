(ns tesseract.element)

(defn process-props
  "Process incoming element properties"
  [props]
  {:attrs props})

(defmacro defelement
  [tag]
  `(def ~(symbol (str *ns*) (str tag))
     (fn [props# & children#]
       (merge {:tag ~(keyword tag)
               :attrs {}
               :children (or children# [])}
              (process-props props#)))))
