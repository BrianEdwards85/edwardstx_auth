(ns us.edwardstx.auth.data.db
  (:require [hikari-cp.core :refer :all]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defrecord Database [conf connection]
  component/Lifecycle

  (start [this]
    (let [db-conf (-> conf :conf :db)]
      (println db-conf)
      (log/info "Connecting to database: " (assoc db-conf :password "*********"))
      (let [ds (make-datasource db-conf)]
        (log/info "Connected to database")
        ;; (if-let [init-fn (:init-fn options)] (init-fn ds))
        (assoc this :connection ds))))

  (stop [this]
    (log/info "Disconnecting from database")
    (close-datasource connection)
    (assoc this :connection nil)))


(defn new-database []
  (map->Database {}))

(defn get-connection [db]
  {:connection (select-keys db [:connection])})
