(ns todomvc.core
  (:require [tesseract.core :as tesseract :refer-macros [defcomponent]]))

(defrecord TodoItem [id description completed])

(let [counter (atom 0)]
  (defn ^:private unique-identifier []
    (swap! counter inc)))

(defn ^:private make-todo
  [description done]
  (TodoItem. (unique-identifier) description completed))

(defcomponent Todo
  (default-state
    {:has-focus false})
  (render [{:keys [attrs state] :as component}]
          (let [{:keys [description completed] :as todo} (attrs :item)
                update-todo (attrs :update-todo)]
            (dom/li {:class {:todo true
                             :focused (state :has-focus)
                             :complete completed}}
                    (dom/input {:type :checkbox
                                :checked completed
                                :on-change #(update-todo todo {:description description
                                                               :completed (-> % .-target .-checked)})})
                    (dom/input {:type :text
                                :value description
                                :on-focus #(tesseract/set-state! component (assoc state :has-focus true))
                                :on-blur #(tesseract/set-state! component (assoc state :has-focus false))
                                :on-change #(update-todo todo {:description (-> % .-target .-value)
                                                               :completed completed})})))))

(defcomponent TodoList
  (default-state
    {:todos {}})
  (on-update-attrs [{old-attrs :attrs :as component} new-attrs]
                   ; flip the incoming todos to be a hash of id => todo
                   (tesseract/set-state! component
                                         {:todos (reduce #(assoc %1 (:id %2) %2) {} (:todos new-attrs))}))
  (render [component]
          (let [update-todo (fn [todo new-values]
                              (tesseract/set-state! component
                                                    (assoc-in component [:state :todos (:id todo)]
                                                              (merge todo new-values))))]
            (dom/ul {:class :todos}
                    ; TODO: This syntax is going to be fairly common (map Component some-seq)
                    ; so maybe components should optionally accept non-hash arguments and assume
                    ; they are a value for the :item key of the :attrs hash?
                    ; It would be nice to write (map Todo (-> component :state :todos))
                    (map #(Todo {:item %
                                 :update-todo update-todo})
                         (-> component :state :todos))))))

(set! (.-onload js/window)
      (fn [e]
        (tesseract/attach!
         (TodoList {:todos [(make-todo "Go buy things" true)
                            (make-todo "Take a nap" false)
                            (make-todo "Reddit" false)]})
         js/document.body)))