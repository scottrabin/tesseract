(ns tesseract.core.set-state-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.component :as component]
            [tesseract.mount :as mount]
            [tesseract.dom :as dom]))

(defcomponent Comment
  (render [{:keys [attrs children]}]
    (dom/div {:class :comment}
             (dom/h2 {:class :comment-author} (:author attrs))
             children)))

(defcomponent CommentList
  (render [{{:keys [comments]} :attrs}]
          (dom/div {:class :comment-list}
                   (for [{:keys [author text]} comments]
                     (Comment {:author author} text)))))

(defcomponent CommentBox
  (will-mount! [this]
              (core/set-state! this
                               {:comments [{:author "Logan Linn"
                                            :text "This is one comment"}
                                           {:author "Scott Rabin"
                                            :text "This is *another* comment"}]})
              this)
  (render [{:keys [state] :as this}]
          (dom/div {:class :comment-box}
                   (dom/h1 {} "Comments")
                   (CommentList {:comments (:comments state)}))))

(deftest test-comment-box
  (testing "set state on will-mount!"
    (core/unmount-all!) ;; reset testing env
    (let [c (CommentBox {})
          container (.createElement js/document "div")]
      (.setAttribute container "id" "test-comment-box")
      (.appendChild js/document.body container)
      (core/mount-into-container! c container)
      (testing "Before processing set-state!"
        (is (= "<div class=\"comment-box\"><h1>Comments</h1><div class=\"comment-list\"></div></div>"
               (.-innerHTML container))))
      (core/flush-next-state!) ;; process our set-state! side-effects
      (testing "After processing set-state!"
        (is (= "<div class=\"comment-box\"><h1>Comments</h1><div class=\"comment-list\"><div class=\"comment\"><h2 class=\"comment-author\">Logan Linn</h2>This is one comment</div><div class=\"comment\"><h2 class=\"comment-author\">Scott Rabin</h2>This is *another* comment</div></div></div>"
               (.-innerHTML container)))))))
