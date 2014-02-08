(ns tesseract.mount
  "Handles mounting components into and out of env, a map ref")

(def ROOT_ID_ATTR "data-tesseractid")
(def DOCUMENT_NODE)

(defprotocol IMount
  (mount! [this root-id container])
  (unmount! [this root-id container]))

(defprotocol IMountUpdate
  (update-mount! [this new-attrs]))

(defn- attr [node k] (.getAttribute node k)) ;; TODO require from somewhere else

(defn- empty-node!
  "http://jsperf.com/emptying-a-node"
  [node]
  (while (.-lastChild node) (.removeChild node (.-lastChild node))))

;; TODO something smarter
(def ^:private id-seq (atom 0))
(defn create-root-id [] (swap! id-seq inc))

(defn root-element [container]
  (if (= (.-nodeType container) DOCUMENT_NODE)
    (.-documentElement container)
    (.-firstChild container)))

(defn root-id [container]
  (attr (root-element container) ROOT_ID_ATTR))

(defn component-by-root-id [env id] (get-in @env [:components id]))

(defn container-by-root-id [env id] (get-in @env [:containers id]))

(defn unregister-root-id!
  "Dissociates root id from containers + components in env"
  [env id]
  (swap! env (fn [{:keys [containers components]}]
               (assoc env
                      :containers (dissoc containers id)
                      :components (dissoc components id)))))

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
      (unmount! component id container)
      (unregister-root-id! env id)
      (empty-node! container)
      true)))

(defn mount-component!
  [env component container]
  (unmount-component! env container) ;; TODO update if already mounted
  (let [id (register-component! env component container)]
    (mount! component id container)))
