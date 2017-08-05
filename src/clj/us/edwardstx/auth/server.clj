(ns us.edwardstx.auth.server
  (:require [aleph.http :as http]
            [com.stuartsierra.component :as component]))


(defrecord Server [handler conf server]
  component/Lifecycle

  (start [this]
    (assoc this
           :server (http/start-server (:http-handler handler) (select-keys (:conf conf) [:port]))))

  (stop [this]
    (.close server)
    (assoc this :server nil))

  )

(defn new-server []
  (map->Server {}))

