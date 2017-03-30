(ns us.edwardstx.auth.authentication-func-test
  (:use midje.sweet)
  (:require [us.edwardstx.auth.authentication :as auth]
            [us.edwardstx.test.db :refer [new-test-database]]
            [com.stuartsierra.component :as component]
            [one-time.core :as ot]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
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

(defn generate-ksr [service]
   {:sub service
    :iss service
    :jti (str (java.util.UUID/randomUUID))
    :exp (time/plus (time/now) (time/minutes 1))})


(defonce system (atom nil))

(against-background [(before :contents (reset! system (init-db db-conf)))
                     (after :contents (component/stop @system))]
                    (facts "authenticate" :func
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
                                   @(auth/authenticate db email password (+ 1 code)) => false)))
                    (facts "authenticate-update-password!" :func
                           (let [db (:db @system)
                                 conf (:conf db)
                                 {:keys [email password secret]} (last (:users conf))
                                 npass (str "new_" password "_updates")
                                 code (ot/get-totp-token secret)]
                             (fact "authenticate-update-password!"
                                   (do
                                     @(auth/authenticate-update-password! db email password npass code)
                                     @(auth/authenticate db email npass code)) => true)))
                    (facts "validate-token" :func
                           (let [db (:db @system)
                                 conf (:conf db)
                                 service-name "hvac_daemon"
                                 service (-> conf :services (get "hvac_daemon"))
                                 ksr (generate-ksr service-name)
                                 token (jwt/sign ksr (:private-key service)  {:alg :es256})
                                 claims (dissoc  (assoc ksr :key (:public-key-base64 service)) :exp )]
                             (dissoc @(auth/validate-token db service-name token) :exp) => claims))




                    )
