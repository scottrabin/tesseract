(ns tesseract.env
  "Tesseract environment controls")

(defn create-env
  "Create a new Tesseract environment"
  []
  (atom {:components {} ; A map of root-id values to the component instances
         :containers {} ; A map of root-id values to the DOM elements component instances are mounted in
         }))

(defn register-container!
  "Registers a DOM element as a mount-target container in the given environment"
  [env container id]
  (swap! env assoc-in [:containers id] container))

(defn register-component!
  "Registers a component under the given ID into the given environment"
  [env component id]
  (swap! env assoc-in [:components id] component))

(defn unregister-root-id!
  "Dissociates root id from containers + components in env"
  [env id]
  (swap! env (fn [{:keys [containers components] :as env}]
               (assoc env
                      :containers (dissoc containers id)
                      :components (dissoc components id)))))
