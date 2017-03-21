(ns us.edwardstx.auth.conf
  (:require [clojure.data.json :as json]
            [us.edwardstx.conf.client :as c]
            [us.edwardstx.auth.keys :as keys]
            [clj-time.core :as time]
            [clojure.spec :as s]
            [com.stuartsierra.component :as component]))

(def env-settings [:service-name :public-key :port ])

(defn create-self-service-token [keys]
  (keys/sign keys
             (keys/extend-claims keys
                                 (keys/creat-claims (-> keys :env :service-name) (str (java.util.UUID/randomUUID)))
                                 {:key (get-in keys [:env :public-key])})))

(defrecord Conf [keys env conf]
  component/Lifecycle

  (start [this]
    (assoc this :conf
           (merge env
                  @(c/get-conf (:service-name env)
                               (create-self-service-token keys)
                               (:key-pair keys)))))

  (stop [this]
    (assoc this :conf nil)))

(defn new-conf [env]
  (map->Conf {:env (select-keys env env-settings)}))



