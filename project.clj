(defproject myospermglyph "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
               [org.clojure/clojurescript "0.0-2197"]
              [org.clojure/math.numeric-tower "0.0.4"]
               [compojure "1.1.5"]
               [jayq "2.5.1"]
              [paddleguru/clutch "0.4.0"]
               [hiccup "1.0.4"]]
:plugins [[lein-cljsbuild "1.0.3"]
          [lein-ring "0.8.11"]]
  :source-paths ["src/clj"]
:cljsbuild {
  :builds {
    :main {
      :source-paths ["src/cljs"],
      :id "dev2",
      :compiler {
        :output-dir "resources/public/auto-js/",
        :output-to "resources/public/auto-js/cljs.js",
        :optimizations :simple,
    ;    :source-map-path "js",
    ;    :source-map "resources/public/auto-js/cljs.js.map",
        :pretty-print true
      }
    }}
    }
  
  :main myospermglyph.server
:ring {:handler myospermglyph.server/app :auto-refresh? true :auto-reload? true}
  )
