(ns tesseract.dom
  (:refer-clojure :exclude [map meta time var])
  (:require [clojure.string])
  #+cljs (:require-macros [tesseract.dom :refer [defelement]]))

(defprotocol IAttributeValue
  (to-attr [this] "Generate an unescaped attribute value string"))

(extend-protocol IAttributeValue
  #+clj String #+cljs string
  (to-attr [this]
    this)

  #+clj clojure.lang.Keyword #+cljs cljs.core/Keyword
  (to-attr [this]
    (name this))

  #+clj clojure.lang.LazySeq #+cljs cljs.core/LazySeq
  (to-attr [this]
    (clojure.string/join " " (clojure.core/map to-attr this)))

  #+clj clojure.lang.PersistentList #+cljs cljs.core/List
  (to-attr [this]
    (to-attr (clojure.core/map to-attr this)))

  #+clj clojure.lang.PersistentVector #+cljs cljs.core/PersistentVector
  (to-attr [this]
    (to-attr (clojure.core/map to-attr this)))

  #+clj clojure.lang.PersistentHashSet #+cljs cljs.core/PersistentHashSet
  (to-attr [this]
    (to-attr (clojure.core/map to-attr this)))

  #+clj clojure.lang.PersistentArrayMap #+cljs cljs.core/PersistentArrayMap
  (to-attr [this]
    (to-attr (for [[k v] this :when v] k)))

  #+clj clojure.lang.PersistentTreeMap #+cljs cljs.core/PersistentTreeMap
  (to-attr [this]
    (to-attr (for [[k v] this :when v] k)))

  #+clj clojure.lang.PersistentTreeSet #+cljs cljs.core/PersistentTreeSet
  (to-attr [this]
    (to-attr (clojure.core/map to-attr this)))

  #+clj java.lang.Boolean #+cljs boolean
  (to-attr [this] (str this))

  #+clj java.lang.Number #+cljs number
  (to-attr [this] (str this)))

(def HTML_ATTR_ESCAPE {\< "&lt;"
                       \> "&gt;"
                       \" "&quot;"
                       \' "&apos;"
                       \& "&amp;"})
(defn to-element-attribute
  "Translate an attribute key value pair to a string"
  ([[k v]]
   (to-element-attribute k v))
  ([k v]
   (str (name k)
        "=\""
        (clojure.string/escape (to-attr v) HTML_ATTR_ESCAPE)
        "\"")))

(defrecord Element [tag attrs children]
  Object
  (toString [_]
    (let [tag-name (-> tag name str)]
      (str
        "<"
        tag-name
        (when-not (empty? attrs)
          (str " " (clojure.string/join " " (clojure.core/map to-element-attribute attrs))))
        ">"
        (when-not (empty? children)
          (clojure.string/join (clojure.core/map str (flatten children))))
        "</" tag-name ">"))))

(defmacro defelement
  [tag]
  (let [tag-kw (keyword tag)]
    `(defn ~tag
       [attrs# & children#]
       (new Element
            ~tag-kw
            attrs#
            (or children# [])))))

; The commented out elements below are deprecated elements. There are others that are still
; available and considered not-best-practice (e.g. <b> and <i>), but they are not officially
; obsolete.
(defelement a)
(defelement abbr)
;(defelement acronym)
(defelement address)
;(defelement applet)
(defelement area)
(defelement article)
(defelement aside)
(defelement audio)
(defelement b)
(defelement base)
;(defelement basefont)
(defelement bdi)
(defelement bdo)
;(defelement bgsound)
;(defelement big)
;(defelement blink)
(defelement blockquote)
(defelement body)
(defelement br)
(defelement button)
(defelement canvas)
(defelement caption)
;(defelement center)
(defelement cite)
(defelement code)
(defelement col)
(defelement colgroup)
;(defelement content)
(defelement data)
(defelement datalist)
(defelement dd)
;(defelement decorator)
(defelement del)
(defelement details)
(defelement dfn)
;(defelement dir)
(defelement div)
(defelement dl)
(defelement dt)
(defelement element)
(defelement em)
(defelement embed)
(defelement fieldset)
(defelement figcaption)
(defelement figure)
;(defelement font)
(defelement footer)
(defelement form)
;(defelement frame)
;(defelement frameset)
(defelement h1)
(defelement h2)
(defelement h3)
(defelement h4)
(defelement h5)
(defelement h6)
(defelement head)
(defelement header)
;(defelement hgroup)
(defelement hr)
(defelement html)
(defelement i)
(defelement iframe)
(defelement img)
(defelement input)
(defelement ins)
;(defelement isindex)
(defelement kbd)
(defelement keygen)
(defelement label)
(defelement legend)
(defelement li)
(defelement link)
;(defelement listing)
(defelement main)
(defelement map)
(defelement mark)
;(defelement marquee)
(defelement menu)
(defelement menuitem)
(defelement meta)
(defelement meter)
(defelement nav)
;(defelement nobr)
;(defelement noframes)
(defelement noscript)
(defelement object)
(defelement ol)
(defelement optgroup)
(defelement option)
(defelement output)
(defelement p)
(defelement param)
;(defelement plaintext)
(defelement pre)
(defelement progress)
(defelement q)
(defelement rp)
(defelement rt)
(defelement ruby)
(defelement s)
(defelement samp)
(defelement script)
(defelement section)
(defelement select)
;(defelement shadow)
(defelement small)
(defelement source)
;(defelement spacer)
(defelement span)
;(defelement strike)
(defelement strong)
(defelement style)
(defelement sub)
(defelement summary)
(defelement sup)
(defelement table)
(defelement tbody)
(defelement td)
;(defelement template)
(defelement textarea)
(defelement tfoot)
(defelement th)
(defelement thead)
(defelement time)
(defelement title)
(defelement tr)
(defelement track)
;(defelement tt)
(defelement u)
(defelement ul)
(defelement var)
(defelement video)
(defelement wbr)
;(defelement xmp)
