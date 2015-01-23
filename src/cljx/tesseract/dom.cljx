(ns tesseract.dom
  (:refer-clojure :exclude [map meta time var])
  #+cljs
  (:require-macros
    [tesseract.dom :refer [defelement]])
  (:require
    [clojure.string]
    [tesseract.impl.vdom :as impl.vdom]
    [tesseract.impl.patch :as impl.patch]
    [tesseract.attrs :as attrs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Patches

(defrecord PatchSetAttributes [attrs]
  impl.patch/IPatch
  (-patch! [_ node]
    (doseq [[attr v] attrs]
      (attrs/set-attribute! attr node v))))

(defrecord PatchSetText [text]
  impl.patch/IPatch
  (-patch! [_ node]
    (set! (. node -textContent) text)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public API

(defrecord Element [tag attrs children]
  impl.vdom/IVirtualNode
  (-mount! [_]
    #+cljs
    (let [el (.createElement js/document (name tag))]
      (doseq [[attr v] attrs]
        (attrs/set-attribute! attr el v))
      (doseq [c children]
        (.appendChild el (impl.vdom/-mount! c)))
      el))
  (-unmount! [_ node]
    #+cljs
    (loop [child-node (.-firstChild node)
           [child-vnode & more] children]
      (impl.vdom/-unmount! child-vnode child-node)
      (when more
        (recur (.-nextSibling child-node) more))))
  (-diff [this next-node]
    (when-let [attr-diff (impl.vdom/diff-map attrs (:attrs next-node))]
      (->PatchSetAttributes attr-diff)))
  (render [this] this)

  impl.vdom/IContainerNode
  (children [_] children)

  Object
  (toString [this]
    (let [tag-name (-> tag name str)]
      (str
        "<"
        tag-name
        (when (seq attrs)
          (->> attrs
               (filter #(satisfies? tesseract.attrs/IAttributeValue (val %)))
               (clojure.core/map tesseract.attrs/to-element-attribute)
               (clojure.string/join " ")
               (str " ")))
        ">"
        (when (seq children)
          (clojure.string/join (clojure.core/map str (flatten children))))
        "</" tag-name ">"))))

#+cljs
(extend-type cljs.core/Keyword
  attrs/IAttribute
  (set-attribute! [this node value]
    (.setAttribute node (name this) (attrs/to-attr value))))

#+cljs
(extend-protocol impl.vdom/IVirtualNode
  string
  (-mount! [this]
    (.createTextNode js/document this))
  (-unmount! [this node])
  (-diff [this other]
    (when-not (= this other)
      (->PatchSetText other)))
  (render [this] this)

  number
  (-mount! [this]
    (.createTextNode js/document (str this)))
  (-unmount! [this node])
  (-diff [this other]
    (when-not (= this other)
      (->PatchSetText (str other))))
  (render [this] this)

  boolean
  (-mount! [this]
    (.createTextNode js/document (str this)))
  (-unmount! [this node])
  (-diff [this other]
    (when-not (= this other)
      (->PatchSetText (str other))))
  (render [this] this)

  nil
  (-mount! [_]
    (.createComment js/document ""))
  (-unmount! [_ _])
  (-diff [this other])
  (render [this] this))

#+cljs
(extend-type js/NodeList
  ISequential
  ISeqable
  (-seq [this]
    (for [i (range (.-length this))]
      (.item this i)))
  ICounted
  (-count [this]
    (.-length this))
  IIndexed
  (-nth [this n]
    (.item this n))
  (-nth [this n not-found]
    (or (.item this n)
        not-found)))

#+cljs
(extend-type js/HTMLElement
  impl.vdom/IRenderNode
  (-insert! [this insert-vnode insert-position]
    (.insertBefore this
                   (impl.vdom/-mount! insert-vnode)
                   (.item (.-childNodes this) insert-position)))
  (-remove! [this remove-vnode remove-position]
    (let [node-to-remove (.item (.-childNodes this) remove-position)]
      (impl.vdom/-unmount! remove-vnode node-to-remove)
      (.removeChild this node-to-remove)))

  impl.vdom/IContainerNode
  (children [this]
    (.-childNodes this)))

#+clj
(defmacro defelement
  [tag]
  (let [tag-kw (keyword tag)]
    `(let [base-element# (new Element ~tag-kw nil nil)]
       (defn ~tag
         ([]
          base-element#)
         ([attrs#]
          (if (nil? attrs#)
            base-element#
            (new Element ~tag-kw attrs# nil)))
         ([attrs# & children#]
          (new Element ~tag-kw attrs# (vec children#)))))))

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
