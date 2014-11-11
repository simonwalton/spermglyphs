(defproject myospermglyph "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2197"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/algo.generic "0.1.2"]
                 [compojure "1.1.5"]
                 [jayq "2.5.1"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [postgresql "9.1-901.jdbc4"]
                 [ring/ring-jetty-adapter "1.2.1"]                 
                 [hiccup "1.0.4"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.11"]]
  :source-paths ["src/clj"]
  :main ^:skip-aot myospermglyph.server
  :uberjar-name "myospermglyph"
  :resource-paths ["resources/"]
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.0.0"
  :cljsbuild {
    :builds {
      :main {
        :source-paths ["src/cljs"],
        :id "dev2",
        :compiler {
          :output-dir "resources/public/auto-js/",
          :output-to "resources/public/auto-js/cljs.js",
          :optimizations :simple,
          :pretty-print true
          }
        }
      }
    }
  :ring {:handler myospermglyph.server/app :auto-refresh? true :auto-reload? true}
)
