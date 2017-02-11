(defproject us.edwardstx/auth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/clojurescript "1.9.293"
                  :scope "provided"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/tools.nrepl "0.2.12"]

                 [aleph "0.4.1"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-defaults "0.2.1"]

                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 [yogthos/config "0.8"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.7"
                  :exclusions [org.clojure/tools.reader]]
                 [reagent "0.6.0"]
                 [reagent-utils "0.2.0"]
                 [re-frame "0.9.2"]
                 [day8.re-frame/http-fx "0.1.3"]

                 [hikari-cp "1.7.5"]
                 [org.postgresql/postgresql "9.4.1208.jre7"]
                 [yesql "0.5.3"]

                 [lock-key "1.4.1"]
                 [digest "1.4.5"]
                 [buddy "1.2.0"]
                 [one-time "0.2.0"]
                 [clj-time "0.11.0"]

                 [com.rpl/specter "0.10.0"]
                 [cljs-http "0.1.42"]

                 [us.edwardstx.conf/client "0.3.2"]
                 [hare "0.2.0"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.1"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler us.edwardstx.auth.handler/app
         :uberwar-name "auth.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "auth.jar"

  :main us.edwardstx.auth.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/uberjar"
              :optimizations :advanced
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel true
             :compiler
             {:main "us.edwardstx.auth.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}



            }
   }


  :figwheel
  {:http-server-root "public"
   :server-port 5002
   :nrepl-port 6002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler us.edwardstx.auth.handler/app}



  :profiles {:dev {:repl-options {:init-ns us.edwardstx.auth
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.5.1"]
                                  [prone "1.1.4"]
                                  [midje "1.8.3" :exclusions [org.clojure/clojure]]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.8.1"]
                                  ]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.8"]
                             [lein-midje "3.2.1"]
                             ]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
