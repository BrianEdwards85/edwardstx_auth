(ns us.edwardstx.auth.authentication-func-test
  (:use midje.sweet)
  (:require [us.edwardstx.auth.authentication :as auth]
            [us.edwardstx.test.db :refer [new-test-database]]
            [com.stuartsierra.component :as component]
            [one-time.core :as ot]
            [manifold.deferred :as d]))

(def db-conf
  {:services {"auth.edwardstx.us" nil
              "conf.edwardstx.us" nil
              "hvac_daemon"       nil}
   :user-count 3})

(defn init-db [conf]
  (component/start
   (component/system-map
    :db (new-test-database conf))))


(defonce system (atom nil))

(against-background [(before :contents (reset! system (init-db db-conf)))
                     (after :contents (component/stop @system))]
                    (facts "authenticate"
                           (let [db (:db @system)
                                 conf (:conf db)
                                 {:keys [email password secret]} (first (:users conf))
                                 code (ot/get-totp-token secret)]
                             (fact "Sucessful authenticate"
                                   @(auth/authenticate db email password code) => true)
                             (fact "Invalid user"
                                   @(auth/authenticate db (str "X"  email) password code) => false)
                             (fact "Invalid password"
                                   @(auth/authenticate db email (str password "C") code) => false)
                             (fact "Invalid auth"
                                   @(auth/authenticate db email password (+ 1 code)) => false))))
