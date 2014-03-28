(ns examples.todomvc.core
  (:require [tesseract.core :as tesseract :include-macros true]
            [tesseract.dom :as dom]))

(defrecord TodoItem [id description done?])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-focus! [component _]
  (tesseract/update-state component assoc-in [:state :focused?] true))

(defn remove-focus! [component _]
  (tesseract/update-state component assoc-in [:state :focused?] false))

(defn update [{{:keys [update-todo item refs]} :attrs :as component} _]
  (update-todo item {:description (.-value (:description refs))
                     :done? (.-value (:completed refs))}))

(tesseract/defcomponent Todo
  (default-state {:focused? false})
  (render [{:keys [attrs state] :as component}]
          (let [{:keys [description done?]} (attrs :item)]
            (dom/li
              {:class {:todo true
                       :focused (:focused? state)
                       :complete done?}}
              (dom/input (cond-> {:type :checkbox
                                  ;:ref :completed
                                  ;:on-change update
                                  }
                           done? (assoc :checked true)))
              (dom/input {:type :text
                          :value description
                          ;:ref :description
                          ;:on-focus set-focus!
                          ;:on-blur remove-focus!
                          ;:on-change update
                          })))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-todo [component todo new-values]
  (tesseract/update-state! component update-in [:todos (:id todo)]
                           merge new-values))

(tesseract/defcomponent TodoList
  (render [{attrs :attrs :as component}]
          (dom/ul {:class :todos}
                  (map #(Todo {:item %}) (:todos attrs)))))

(set! (.-onload js/window)
      (fn [e]
        (tesseract/mount-into-container!
          (TodoList {:todos [(TodoItem. 1 "Go buy things" true)
                             (TodoItem. 2 "Take a nap" false)
                             (TodoItem. 3 "Reddit" false)]})
          js/document.body)))
