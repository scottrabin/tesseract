(ns tesseract.mount-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [tesseract.mount :as mount]
            [cemerick.cljs.test :as t]))

(deftest test-mount-component!
  (let [calls (atom {})
        component-a (reify
                      mount/IMount
                      (mount! [component root-id container]
                        (swap! calls update-in [:a :mount!] (fnil inc 0))
                        (set! (.-innerHTML container) "<div></div>"))
                      (unmount! [component root-id container]
                        (swap! calls update-in [:a :unmount!] (fnil inc 0)))
                      mount/IMountUpdate
                      (update-mount! [this next-component]
                        (swap! calls update-in [:a :update-mount!] (fnil inc 0))))
        component-b (reify
                      mount/IMount
                      (mount! [component root-id container]
                        (swap! calls update-in [:b :mount!] (fnil inc 0))
                        (set! (.-innerHTML container) "<div></div>"))
                      (unmount! [component root-id container]
                        (swap! calls update-in [:b :unmount!] (fnil inc 0)))
                      mount/IMountUpdate
                      (update-mount! [this next-component]
                        (swap! calls update-in [:b :update-mount!] (fnil inc 0))))
        env (atom {})
        container js/document.body]
    (testing "mounting when no component is mounted"
      (mount/mount-component! env component-a container)
      (is (= 1 (get-in @calls [:a :mount!] 0)))
      (is (= 0 (get-in @calls [:a :unmount!] 0)))
      (is (= component-a (mount/component-by-root-id env (mount/root-id container))))
      (is (= container (mount/container-by-root-id env (mount/root-id container)))))

    (testing "mounting a different type of component on container with component"
      (mount/mount-component! env component-b container)
      (is (= 1 (get-in @calls [:a :mount!] 0)))
      (is (= 1 (get-in @calls [:a :unmount!] 0)))
      (is (= 1 (get-in @calls [:b :mount!] 0)))
      (is (= 0 (get-in @calls [:b :unmount!] 0))))
    ))
