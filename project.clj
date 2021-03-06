(defproject us.edwardstx/auth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/clojurescript "1.9.854"
                  :scope "provided"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/tools.logging "0.3.1"]

                 [aleph "0.4.3"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-defaults "0.2.1"]

                 [binaryage/devtools "0.9.0"]

                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 [yogthos/config "0.8"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.rpl/specter "0.10.0"]

                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.7"
                  :exclusions [org.clojure/tools.reader]]
                 [com.cemerick/url "0.1.1"]
                 [reagent "0.6.0"]
                 [reagent-utils "0.2.0"]
                 [re-frame "0.9.2"]
                 [re-com "0.9.0"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [cljs-ajax "0.5.8"]

                 [hikari-cp "1.7.5"]
                 [org.postgresql/postgresql "9.4.1208.jre7"]
                 [yesql "0.5.3"]

                 [buddy "1.2.0"]
                 [one-time "0.2.0"]
                 [clj-crypto "1.0.2"
                  :exclusions [org.bouncycastle/bcprov-jdk15on bouncycastle/bcprov-jdk16]]
                 [clj-time "0.11.0"]


                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.logging.log4j/log4j-core "2.7"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.7"]
                 [org.springframework.amqp/spring-rabbit "2.0.0.M2"
                  :exclusions [org.springframework/spring-web org.springframework/spring-tx]]

                 [us.edwardstx/conf-client "0.4.5"]
                 ;;[hare "0.2.0"]
                 ]

  :plugins [[lein-environ "1.0.2"]
            [lein-sassy "1.0.8"]
            [lein-cljsbuild "1.1.7"]
            ]

  :repositories [["spring.milestone" "https://repo.spring.io/libs-milestone"]]

  :min-lein-version "2.5.0"

  :uberjar-name "auth.jar"

  :main us.edwardstx.auth

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]


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
              :asset-path "/auth/assets/js/out"
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
                                  [re-frisk "0.3.0"]
                                  [prone "1.1.4"]
                                  [midje "1.9.0-alpha6" :exclusions [org.clojure/clojure]]
                                  [org.clojure/test.check "0.9.0"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.8.1"]
                                  [com.h2database/h2 "1.4.194"]
                                  [org.clojure/java.jdbc "0.6.1"]]

                   :resource-paths ["env/dev/resources" "env/test/resources" "resources"]
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
