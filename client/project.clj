(defproject us.edwardstx/auth-client "0.1.0-SNAPSHOT"
  :description "Edwardstx Auth client"
  :url "https://github.com/BrianEdwards85/edwardstx_auth"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [aleph "0.4.1"]
                 [buddy "1.2.0"]
                 [clj-crypto "1.0.2"
                  :exclusions [org.bouncycastle/bcprov-jdk15on bouncycastle/bcprov-jdk16]]
                 [overtone/at-at "1.2.0"]
                 [clj-time "0.11.0"]])

