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
  :source-paths ["target/classes"]
  :test-paths ["target/test-classes"]
  :cljx {:builds
         [{:source-paths ["src/cljx"]
           :output-path "target/classes"
           :rules :clj}
          {:source-paths ["src/cljx"]
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
               {:source-paths ["target/classes"]
                :compiler {:output-to "resources/public/js/main.js"
                           :optimizations :none
                           :pretty-print true}}
               :test
               {:source-paths ["target/classes" "target/test-classes"]
                :compiler {:output-to "target/generated/cljs-test.js"
                           :optimizations :whitespace ; clojurescript.test cannot work with :optimizations :none
                           :pretty-print true}}}})
