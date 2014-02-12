(ns tesseract.mount
  "Handles mounting components into and out of env, a map ref"
  (:require [tesseract.component :as component]))

(def ROOT_ID_ATTR "data-tesseractid")
(def DOCUMENT_NODE)

(defn- attr [node k] (.getAttribute node k)) ;; TODO require from somewhere else
(defn- set-attr! [node k v] (.setAttribute node k v))

(defn- empty-node!
  "http://jsperf.com/emptying-a-node"
  [node]
  (while (.-lastChild node) (.removeChild node (.-lastChild node))))

;; TODO something smarter
(def ^:private id-seq (atom 0))
(defn create-root-id [] (str (swap! id-seq inc)))

(defn root-element [container]
  (if (= (.-nodeType container) DOCUMENT_NODE)
    (.-documentElement container)
    (.-firstChild container)))

(defn root-id [container]
  (when-let [el (root-element container)]
    (attr el ROOT_ID_ATTR)))

(defn component-by-root-id [env id] (get-in @env [:components id]))

(defn container-by-root-id [env id] (get-in @env [:containers id]))

(defn unregister-root-id!
  "Dissociates root id from containers + components in env"
  [env id]
  (swap! env (fn [{:keys [containers components] :as env}]
               (assoc env
                      :containers (if containers (dissoc containers id))
                      :components (if components (dissoc components id))))))

(defn register-container!
  [env container]
  (let [id (or (root-id container)
               (create-root-id))]
    (swap! env assoc-in [:containers id] container)
    id))

(defn register-component!
  [env component container]
  (let [id (register-container! env container)]
    (swap! env assoc-in [:components id] component)
    id))

(defn unmount-component! [env container]
  (when-let [id (root-id container)]
    (when-let [component (component-by-root-id env id)]
      (when (satisfies? component/IWillUnmount component)
        (component/-will-unmount component)) ;; TODO probably needs try/catch?
      (unregister-root-id! env id)
      (empty-node! container)
      true)))

(defn- mount-into-container!
  [component id container]
  (set! (.-innerHTML container) (-> component component/-render str))
  (if-let [el (root-element container)]
    (do
      (set-attr! el ROOT_ID_ATTR id)
      (when (satisfies? component/IDidMount component)
        (component/-did-mount component container)))
    (throw (js/Error. "No root element detected on mounted component"))))

(defn mount-component!
  [env component container]
  (let [existing-id (root-id container)
        existing-component (when existing-id (component-by-root-id env existing-id))]
    (if (and existing-component
             (= (type existing-component) (type component)))
      (component/update existing-component component)
      (do
        (when existing-id
          (unmount-component! env container))
        (let [id (register-component! env component container)]
          (mount-into-container! component id container))))))
