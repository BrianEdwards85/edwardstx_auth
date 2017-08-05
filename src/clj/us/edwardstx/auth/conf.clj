(ns us.edwardstx.auth.conf
  (:require [clojure.data.json :as json]
 ;;           [us.edwardstx.conf.client :as c]
            [us.edwardstx.auth.keys :as keys]
            [clj-time.core :as time]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]

            [manifold.deferred :as d]
            [clj-crypto.core :as crypto]
            [byte-streams :as bs]
            [aleph.http :as http]
            ))

(def env-settings [:service-name :conf-host :port])

(def ec-cipher (crypto/create-cipher "ECIES"))

(defn create-self-service-token [keys]
  (keys/sign keys
             (keys/extend-claims keys
                                 (merge
                                  (keys/creat-claims (-> keys :env :service-name) (str (java.util.UUID/randomUUID)))
                                  {:key (get-in keys [:env :public-key])}))))

(defn get-conf [host service token key]
  (d/chain
   (http/post (str host "/api/v1/conf/" service) {:body token})
   :body
   bs/to-string
   crypto/decode-base64
   #(crypto/decrypt key % ec-cipher)
   #(json/read-str % :key-fn keyword)))

(defrecord Conf [keys env conf]
  component/Lifecycle

  (start [this]
    (assoc this :conf
           (merge env
                  @(get-conf   (:conf-host env)
                               (:service-name env)
                               (create-self-service-token keys)
                               (:key-pair keys)))))

  (stop [this]
    (assoc this :conf nil)))

(defn new-conf [env]
  (map->Conf {:env (select-keys env env-settings)}))



