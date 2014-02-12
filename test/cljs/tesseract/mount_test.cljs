(ns tesseract.mount-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [tesseract.mount :as mount]
            [tesseract.dom :as dom]
            [tesseract.component :as component]
            [cemerick.cljs.test :as t]))

(deftest test-mount-component!
  (let [calls (atom {})
        container js/document.body
        component-a (reify
                      component/IComponent
                      (-render [component]
                        (swap! calls update-in [:a :render] (fnil inc 0))
                        (dom/div {}))
                      (-update [component new-component]
                        (swap! calls update-in [:a :update] (fnil inc 0)))
                      component/IShouldUpdate
                      (-should-update? [component next-component] true)
                      component/IDidMount
                      (-did-mount [component container]
                        (swap! calls update-in [:a :did-mount] (fnil inc 0)))
                      component/IWillUnmount
                      (-will-unmount [component]
                        (swap! calls update-in [:a :will-unmount] (fnil inc 0))))
        component-b (reify
                      component/IComponent
                      (-render [component]
                        (swap! calls update-in [:b :render] (fnil inc 0))
                        (dom/div {}))
                      (-update [component new-component]
                        (swap! calls update-in [:b :update] (fnil inc 0)))
                      component/IShouldUpdate
                      (-should-update? [component next-component] true)
                      component/IDidMount
                      (-did-mount [component container]
                        (swap! calls update-in [:b :did-mount] (fnil inc 0)))
                      component/IWillUnmount
                      (-will-unmount [component]
                        (swap! calls update-in [:b :will-unmount] (fnil inc 0))))
        env (atom {})]
    (testing "mounting when no component is mounted"
      (mount/mount-component! env component-a container)
      (is (= 1 (get-in @calls [:a :render] 0)))
      (is (= 0 (get-in @calls [:a :update] 0)))
      (is (= 1 (get-in @calls [:a :did-mount] 0)))
      (is (= 0 (get-in @calls [:a :will-unmount] 0)))
      (is (= 0 (get-in @calls [:b :render] 0)))
      (is (= 0 (get-in @calls [:b :update] 0)))
      (is (= 0 (get-in @calls [:b :did-mount] 0)))
      (is (= 0 (get-in @calls [:b :will-unmount] 0)))
      ;; TODO test update calls
      (is (= component-a (mount/component-by-root-id env (mount/root-id container))))
      (is (= container (mount/container-by-root-id env (mount/root-id container)))))

    (testing "mounting a different type of component on container with component"
      (mount/mount-component! env component-b container)
      (is (= 1 (get-in @calls [:a :render] 0)))
      (is (= 0 (get-in @calls [:a :update] 0)))
      (is (= 1 (get-in @calls [:a :did-mount] 0)))
      (is (= 1 (get-in @calls [:a :will-unmount] 0)))
      (is (= 1 (get-in @calls [:b :render] 0)))
      (is (= 1 (get-in @calls [:b :did-mount] 0)))
      (is (= 0 (get-in @calls [:b :will-unmount] 0)))
      (is (= 0 (get-in @calls [:b :update] 0)))
      ;; TODO test update calls
      (is (= component-b (mount/component-by-root-id env (mount/root-id container))))
      (is (= container (mount/container-by-root-id env (mount/root-id container))))
      )
    (testing "mounting same type of component on container with component"
      (mount/mount-component! env component-b container)
      (is (= 1 (get-in @calls [:a :render] 0)))
      (is (= 0 (get-in @calls [:a :update] 0)))
      (is (= 1 (get-in @calls [:a :did-mount] 0)))
      (is (= 1 (get-in @calls [:a :will-unmount] 0)))
      (is (= 1 (get-in @calls [:b :render] 0)))
      (is (= 1 (get-in @calls [:b :did-mount] 0)))
      (is (= 0 (get-in @calls [:b :will-unmount] 0)))
      (is (= 1 (get-in @calls [:b :update] 0))))))
