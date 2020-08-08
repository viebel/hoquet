(defproject viebel/hoquet "0.0.1"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 [net.cgrand/macrovich "0.2.1"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.11"]]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :compiler     {:output-to     "target/cljs/dev/dev.js"
                                       :output-dir    "target/cljs/dev/out"
                                       :optimizations :whitespace
                                       :pretty-print  true}}
                       {:id           "test"
                        :source-paths ["src" "test"]
                        :compiler     {:output-to     "target/cljs/test/test.js"
                                       :output-dir    "target/cljs/test/out"
                                       :main          hoquet.test-runner
                                       :optimizations :none}}

                       {:id           "test-node"
                        :source-paths ["src" "test"]
                        :compiler     {:target        :nodejs
                                       :output-to     "target/cljs/test-node/test.js"
                                       :output-dir    "target/cljs/test-node/out"
                                       :main          hoquet.test-runner
                                       :optimizations :none}}]}
  :aliases {"test-node"    ["doo" "node" "test-node" "once"]
            "test-planck"  ["doo" "planck" "test" "once"]
            "test-all"     ["do" "test," "test-node," "test-planck"]})
