(ns todomvc.core
  (:require [tesseract.core :as tesseract :refer-macros [defcomponent]]))

(defrecord TodoItem [id description completed])

(let [counter (atom 0)]
  (defn ^:private unique-identifier []
    (swap! counter inc)))

(defn ^:private make-todo
  [description done]
  (TodoItem. (unique-identifier) description completed))

(defn set-focus [component _]
  (tesseract/set-state! component (assoc (:state component) :has-focus true)))

(defn remove-focus [component _]
  (tesseract/set-state! component (assoc (:state component) :has-focus false)))

(defn update [{{:keys [update-todo item refs]} :attrs :as component} _]
  (update-todo item {:description (.-value (:description refs))
                     :completed (.-value (:completed refs))}))

(defcomponent Todo
  (default-state
    {:has-focus false})
  (render [{:keys [attrs state] :as component}]
          (let [{:keys [description completed]} (attrs :item)]
            (dom/li {:class {:todo true
                             :focused (state :has-focus)
                             :complete completed}}
                    (dom/input {:type :checkbox
                                :checked completed
                                :ref :completed
                                :on-change update})
                    (dom/input {:type :text
                                :value description
                                :ref :description
                                :on-focus set-focus
                                :on-blur remove-focus
                                :on-change update})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcomponent TodoList
  (bound-methods
    {:update-todo (fn update-todo [component todo new-values]
                    (tesseract/set-state! component
                                          (update-in (:state component)
                                                     [:todos (:id todo)]
                                                     merge new-values)))})
  (default-state
    {:todos {}})
  (on-update-attrs [{old-attrs :attrs :as component} new-attrs]
                   ; flip the incoming todos to be a hash of id => todo
                   (tesseract/set-state! component
                                         {:todos (reduce #(assoc %1 (:id %2) %2) {} (:todos new-attrs))}))
  (render [{:keys [state] :as component}]
          (dom/ul {:class :todos}
                  ; TODO: This syntax is going to be fairly common (map Component some-seq)
                  ; so maybe components should optionally accept non-hash arguments and assume
                  ; they are a value for the :item key of the :attrs hash?
                  ; It would be nice to write (map Todo (-> component :state :todos))
                  (map #(Todo {:item %
                               :update-todo (get-in component [:bound-methods :update-todo])})
                       (:todos state)))))


(set! (.-onload js/window)
      (fn [e]
        (tesseract/attach!
         (TodoList {:todos [(make-todo "Go buy things" true)
                            (make-todo "Take a nap" false)
                            (make-todo "Reddit" false)]})
         js/document.body)))
