(ns tesseract.dom-test
  #+clj (:require [tesseract.dom :as dom :refer [defelement]]
                  [clojure.test :refer :all])
  #+cljs (:require [tesseract.dom :as dom]
                   [cemerick.cljs.test :as t])
  #+cljs (:require-macros [tesseract.dom :refer [defelement]]
                          [cemerick.cljs.test :refer [is deftest testing]]))

(defn attr=
  "Compare attribute values to check equality"
  [& attrvals]
  (apply = (map #(set (clojure.string/split % #"\s")) attrvals)))

(deftest to-attr
  (testing "Keywords"
    (is (attr= "value"
               (dom/to-attr :value))))
  (testing "Strings"
    (is (attr= "attribute-value"
               (dom/to-attr "attribute-value"))))
  (testing "Lists"
    (is (attr= "first second third"
               (dom/to-attr (list :first "second" "third")))))
  (testing "Vectors"
    (is (attr= "first second third"
               (dom/to-attr ["first" "second" "third"])))
    (is (attr= "keyword second third"
               (dom/to-attr [:keyword "second" "third"])))
    (is (attr= "hash-one hash-two second keyword"
               (dom/to-attr [{:hash-one true
                               :hash-two true}
                              "second"
                              :keyword]))))
  (testing "Hash maps"
    (is (attr= "first second"
               (dom/to-attr {"first" true
                              "second" true})))
    (is (attr= "second only"
               (dom/to-attr {"first" false
                              "second" true
                              "only" true})))
    (is (attr= "vector key other"
               (dom/to-attr {["vector" "key"] true
                              "other" true})))
    (is (attr= "keyword other"
               (dom/to-attr {:keyword true
                              "other" true}))))
  (testing "Sets"
    (is (attr= "first second"
               (dom/to-attr #{:first "second"}))))
  (testing "Sorted map"
    ; attribute value should come out in the sorted order
    (is (= "first second"
           (dom/to-attr (sorted-map-by
                           #(compare (name %1) (name %2))
                           :first true "second" true :third false)))))
  (testing "Sorted set"
    ; attribute value should come out in the sorted order
    (is (= "first second"
           (dom/to-attr (sorted-set-by
                           compare
                           :first :second))))))

(deftest test-defelement
  (let [my-element (defelement my-element)]
    (testing "defined value"
      (is (ifn? my-element)))
    (testing "render to string"
      (is (= "<my-element data-attr=\"some value\"></my-element>"
             (str (my-element {:data-attr "some value"})))))))

; Test each element individually; the plan will have some individual logic
; build into certain elements, and it makes more sense to test each one
; individually for now.
(deftest test-element-a
  (is (ifn? dom/a)))
(deftest test-element-abbr
  (is (ifn? dom/abbr)))
(deftest test-element-address
  (is (ifn? dom/address)))
(deftest test-element-area
  (is (ifn? dom/area)))
(deftest test-element-article
  (is (ifn? dom/article)))
(deftest test-element-aside
  (is (ifn? dom/aside)))
(deftest test-element-audio
  (is (ifn? dom/audio)))
(deftest test-element-b
  (is (ifn? dom/b)))
(deftest test-element-base
  (is (ifn? dom/base)))
(deftest test-element-bdi
  (is (ifn? dom/bdi)))
(deftest test-element-bdo
  (is (ifn? dom/bdo)))
(deftest test-element-blockquote
  (is (ifn? dom/blockquote)))
(deftest test-element-body
  (is (ifn? dom/body)))
(deftest test-element-br
  (is (ifn? dom/br)))
(deftest test-element-button
  (is (ifn? dom/button)))
(deftest test-element-canvas
  (is (ifn? dom/canvas)))
(deftest test-element-caption
  (is (ifn? dom/caption)))
(deftest test-element-cite
  (is (ifn? dom/cite)))
(deftest test-element-code
  (is (ifn? dom/code)))
(deftest test-element-col
  (is (ifn? dom/col)))
(deftest test-element-colgroup
  (is (ifn? dom/colgroup)))
(deftest test-element-data
  (is (ifn? dom/data)))
(deftest test-element-datalist
  (is (ifn? dom/datalist)))
(deftest test-element-dd
  (is (ifn? dom/dd)))
(deftest test-element-del
  (is (ifn? dom/del)))
(deftest test-element-details
  (is (ifn? dom/details)))
(deftest test-element-dfn
  (is (ifn? dom/dfn)))
(deftest test-element-div
  (is (ifn? dom/div)))
(deftest test-element-dl
  (is (ifn? dom/dl)))
(deftest test-element-dt
  (is (ifn? dom/dt)))
(deftest test-element-element
  (is (ifn? dom/element)))
(deftest test-element-em
  (is (ifn? dom/em)))
(deftest test-element-embed
  (is (ifn? dom/embed)))
(deftest test-element-fieldset
  (is (ifn? dom/fieldset)))
(deftest test-element-figcaption
  (is (ifn? dom/figcaption)))
(deftest test-element-figure
  (is (ifn? dom/figure)))
(deftest test-element-footer
  (is (ifn? dom/footer)))
(deftest test-element-form
  (is (ifn? dom/form)))
(deftest test-element-h1
  (is (ifn? dom/h1)))
(deftest test-element-h2
  (is (ifn? dom/h2)))
(deftest test-element-h3
  (is (ifn? dom/h3)))
(deftest test-element-h4
  (is (ifn? dom/h4)))
(deftest test-element-h5
  (is (ifn? dom/h5)))
(deftest test-element-h6
  (is (ifn? dom/h6)))
(deftest test-element-head
  (is (ifn? dom/head)))
(deftest test-element-header
  (is (ifn? dom/header)))
(deftest test-element-hr
  (is (ifn? dom/hr)))
(deftest test-element-html
  (is (ifn? dom/html)))
(deftest test-element-i
  (is (ifn? dom/i)))
(deftest test-element-iframe
  (is (ifn? dom/iframe)))
(deftest test-element-img
  (is (ifn? dom/img)))
(deftest test-element-input
  (is (ifn? dom/input)))
(deftest test-element-ins
  (is (ifn? dom/ins)))
(deftest test-element-kbd
  (is (ifn? dom/kbd)))
(deftest test-element-keygen
  (is (ifn? dom/keygen)))
(deftest test-element-label
  (is (ifn? dom/label)))
(deftest test-element-legend
  (is (ifn? dom/legend)))
(deftest test-element-li
  (is (ifn? dom/li)))
(deftest test-element-link
  (is (ifn? dom/link)))
(deftest test-element-main
  (is (ifn? dom/main)))
(deftest test-element-map
  (is (ifn? dom/map)))
(deftest test-element-mark
  (is (ifn? dom/mark)))
(deftest test-element-menu
  (is (ifn? dom/menu)))
(deftest test-element-menuitem
  (is (ifn? dom/menuitem)))
(deftest test-element-meta
  (is (ifn? dom/meta)))
(deftest test-element-meter
  (is (ifn? dom/meter)))
(deftest test-element-nav
  (is (ifn? dom/nav)))
(deftest test-element-noscript
  (is (ifn? dom/noscript)))
(deftest test-element-object
  (is (ifn? dom/object)))
(deftest test-element-ol
  (is (ifn? dom/ol)))
(deftest test-element-optgroup
  (is (ifn? dom/optgroup)))
(deftest test-element-option
  (is (ifn? dom/option)))
(deftest test-element-output
  (is (ifn? dom/output)))
(deftest test-element-p
  (is (ifn? dom/p)))
(deftest test-element-param
  (is (ifn? dom/param)))
(deftest test-element-pre
  (is (ifn? dom/pre)))
(deftest test-element-progress
  (is (ifn? dom/progress)))
(deftest test-element-q
  (is (ifn? dom/q)))
(deftest test-element-rp
  (is (ifn? dom/rp)))
(deftest test-element-rt
  (is (ifn? dom/rt)))
(deftest test-element-ruby
  (is (ifn? dom/ruby)))
(deftest test-element-s
  (is (ifn? dom/s)))
(deftest test-element-samp
  (is (ifn? dom/samp)))
(deftest test-element-script
  (is (ifn? dom/script)))
(deftest test-element-section
  (is (ifn? dom/section)))
(deftest test-element-select
  (is (ifn? dom/select)))
(deftest test-element-small
  (is (ifn? dom/small)))
(deftest test-element-source
  (is (ifn? dom/source)))
(deftest test-element-span
  (is (ifn? dom/span)))
(deftest test-element-strong
  (is (ifn? dom/strong)))
(deftest test-element-style
  (is (ifn? dom/style)))
(deftest test-element-sub
  (is (ifn? dom/sub)))
(deftest test-element-summary
  (is (ifn? dom/summary)))
(deftest test-element-sup
  (is (ifn? dom/sup)))
(deftest test-element-table
  (is (ifn? dom/table)))
(deftest test-element-tbody
  (is (ifn? dom/tbody)))
(deftest test-element-td
  (is (ifn? dom/td)))
(deftest test-element-textarea
  (is (ifn? dom/textarea)))
(deftest test-element-tfoot
  (is (ifn? dom/tfoot)))
(deftest test-element-th
  (is (ifn? dom/th)))
(deftest test-element-thead
  (is (ifn? dom/thead)))
(deftest test-element-time
  (is (ifn? dom/time)))
(deftest test-element-title
  (is (ifn? dom/title)))
(deftest test-element-tr
  (is (ifn? dom/tr)))
(deftest test-element-track
  (is (ifn? dom/track)))
(deftest test-element-u
  (is (ifn? dom/u)))
(deftest test-element-ul
  (is (ifn? dom/ul)))
(deftest test-element-var
  (is (ifn? dom/var)))
(deftest test-element-video
  (is (ifn? dom/video)))
(deftest test-element-wbr
  (is (ifn? dom/wbr)))

(deftest element-to-string
  (testing "rendered elements"
    (is (= "<div></div>"
           (str (dom/div {}))))
    (is (= "<div class=\"some-class\"></div>"
           (str (dom/div {:class :some-class}))))
    (is (= "<div class=\"parent\"><span class=\"child\"></span></div>"
           (str (dom/div {:class "parent"}
                         (dom/span {:class "child"})))))
    (is (= "<div>Arbitrary text here</div>"
           (str (dom/div {} "Arbitrary text here")))))
  (testing "escaping attributes"
    (is (= "<div class=\"&lt;&gt;&quot;&apos;&amp;\"></div>"
           (str (dom/div {:class "<>\"'&"}))))))

(deftest recurses-children
  (is (= "<div><span>0</span><span>1</span></div>"
         (str (dom/div {} (for [i (range 2)]
                            (dom/span {} i)))))))
