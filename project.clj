(defproject tesseract "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2411"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [com.keminglabs/cljx "0.5.0"]
            [com.cemerick/clojurescript.test "0.3.3"]]

  :source-paths ["src/cljx" "target/classes"]
  :test-paths ["target/test-classes"]

  :cljx
  {:builds
   [{:source-paths ["src/cljx"]  :rules :clj  :output-path "target/classes"}
    {:source-paths ["src/cljx"]  :rules :cljs :output-path "target/classes"}
    {:source-paths ["test/cljx"] :rules :clj  :output-path "target/test-classes"}
    {:source-paths ["test/cljx"] :rules :cljs :output-path "target/test-classes"}]}

  :cljsbuild
  {:test-commands {"unit" ["phantomjs" :runner "target/generated/cljs-test.js"]}
   :builds
   {:test
    {:source-paths ["src/cljs" "test/cljs" "target/classes" "target/test-classes"]
     :compiler {:output-to "target/generated/cljs-test.js"
                ;; clojurescript.test cannot work with :optimizations :none
                :optimizations :whitespace
                :pretty-print true}}}}

  :aliases {"test-all" ["do"
                        "cljx" "once,"
                        "cljsbuild" "once" "test,"
                        "test,"
                        "cljsbuild test"]
            "test-clj" ["do"
                        "cljx" "once,"
                        "test"]
            "test-cljs" ["do"
                         "cljx" "once,"
                         "cljsbuild" "once" "test,"
                         "cljsbuild" "test"]})
