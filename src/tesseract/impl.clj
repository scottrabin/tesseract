(ns tesseract.impl)

(defprotocol IRender
  (render [item]
          "Render an item into a string"))

(extend-protocol IRender
  String
  (render [string]
    string))
