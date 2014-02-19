(defproject tesseract "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]]
  :plugins [[lein-cljsbuild "1.0.2"]
            [com.keminglabs/cljx "0.3.2"]
            [com.cemerick/clojurescript.test "0.2.2"]]
  :hooks [leiningen.cljsbuild cljx.hooks]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
  :source-paths ["target/classes"]
  :test-paths ["target/test-classes"]
  :cljx {:builds
         [{:source-paths ["src/cljx"]
           :output-path "target/classes"
           :rules :clj}
          {:source-paths ["src/cljx" "src/cljs"]
           :output-path "target/classes"
           :rules :cljs}
          {:source-paths ["src/cljx" "test/cljx"]
           :output-path "target/test-classes"
           :rules :clj}
          {:source-paths ["src/cljx" "test/cljx"]
           :output-path "target/test-classes"
           :rules :cljs}]}
  :cljsbuild {:test-commands {"unit" ["phantomjs" :runner
                                      "target/generated/cljs-test.js"]}
              :builds
              {:dev
               {:source-paths ["src/cljs" "test/cljs" "target/classes"]
                :compiler {:output-to "resources/dev/js/main.js"
                           :output-dir "resources/dev/js"
                           :optimizations :none
                           :pretty-print true}}
               :test
               {:source-paths ["src/cljs" "test/cljs" "target/classes" "target/test-classes"]
                :compiler {:output-to "target/generated/cljs-test.js"
                           :optimizations :whitespace ; clojurescript.test cannot work with :optimizations :none
                           :pretty-print true}}

               :basic
               {:source-paths ["src/cljs" "target/classes" "examples/basic/src"]
                :compiler {:output-to "examples/basic/main.js"
                           :output-dir "examples/basic/out"
                           ;:source-map true
                           :optimizations :none}}}})
