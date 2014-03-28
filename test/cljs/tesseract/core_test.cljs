(ns tesseract.core-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [tesseract.core :as core :refer-macros [defcomponent]]
            [tesseract.component :as component]
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
  (initial-state {:comments []})
  (will-mount! [this]
               (update-in this [:state :comments] concat
                          [{:author "Logan Linn"
                            :text "This is one comment"}
                           {:author "Scott Rabin"
                            :text "This is *another* comment"}]))
  (render [{:keys [state] :as this}]
          (dom/div {:class :comment-box}
                   (dom/h1 {} "Comments")
                   (CommentList {:comments (:comments state)}))))

(deftest test-comment-box
  (testing "Updating state in will-mount!"
    (let [c (CommentBox {})
          container (.createElement js/document "div")
          container-id "test-comment-box"]
      (.setAttribute container "id" container-id)
      (.appendChild js/document.body container)
      (core/mount-into-container! c container)
      (is (= "<div class=\"comment-box\"><h1>Comments</h1><div class=\"comment-list\"><div class=\"comment\"><h2 class=\"comment-author\">Logan Linn</h2>This is one comment</div><div class=\"comment\"><h2 class=\"comment-author\">Scott Rabin</h2>This is *another* comment</div></div></div>"
             (.-innerHTML (.getElementById js/document container-id)))))))

(defcomponent CommentBox2
  (initial-state {:comments []})
  (will-mount! [this]
               (core/update-state! this update-in [:comments] concat
                                   [{:author "Logan Linn"
                                     :text "This is one comment"}
                                    {:author "Scott Rabin"
                                     :text "This is *another* comment"}])
               this)
  (render [{:keys [state] :as this}]
          (dom/div {:class :comment-box}
                   (dom/h1 {} "Comments")
                   (CommentList {:comments (:comments state)}))))

(deftest test-comment-box2
  (testing "Calling set-state! in will-mount!"
    (let [c (CommentBox2 {})
          container (.createElement js/document "div")
          container-id "test-comment-box2"]
      (.setAttribute container "id" container-id)
      (.appendChild js/document.body container)
      (core/mount-into-container! c container)
      (testing "Before processing set-state!"
        (is (= "<div class=\"comment-box\"><h1>Comments</h1><div class=\"comment-list\"></div></div>"
               (.-innerHTML (.getElementById js/document container-id)))))
      (core/flush-next-state!) ;; process our set-state! side-effects
      (testing "After processing set-state!"
        (is (= "<div class=\"comment-box\"><h1>Comments</h1><div class=\"comment-list\"><div class=\"comment\"><h2 class=\"comment-author\">Logan Linn</h2>This is one comment</div><div class=\"comment\"><h2 class=\"comment-author\">Scott Rabin</h2>This is *another* comment</div></div></div>"
               (.-innerHTML (.getElementById js/document container-id))))))))
