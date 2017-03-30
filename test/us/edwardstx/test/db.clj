(ns us.edwardstx.test.db
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [us.edwardstx.test.db.core-services :refer [create-services]]
            [us.edwardstx.test.db.auth-credentials :refer [create-credentials]]
            [com.stuartsierra.component :as component]))

(def db-def {:classname   "org.h2.Driver"
             :subprotocol "h2:mem"
             :subname     "test"      ;;"demo;DB_CLOSE_DELAY=-1"
             :user        "sa"
             :password    ""})

(defn get-conn [db]
  {:connection (select-keys db [:connection])})

(defn get-schema-statments [f]
  (->>
   (-> f io/resource io/file slurp (str/split #";"))
   (map str/trim)
   (filter #(-> % empty? not))))

(defn load-schema [conn f]
  (doall
   (map
    #(sql/db-do-commands {:connection  conn} %)
    (get-schema-statments f))))

(defn init-data [db conf]
  (let [conn {:connection {:connection db}}]
    (->> conf
         (create-services conn)
         (create-credentials conn))))

(defrecord Database [conf connection]
  component/Lifecycle

  (start [this]
    (let [db-def (:db-def conf)
          c (sql/get-connection db-def)]
      (load-schema c "sql/schema.sql")

      (assoc this
             :connection c
             :conf (init-data c conf)
             )))

  (stop [this]
    (.close connection)
    (assoc this :connection nil)))


(defn new-test-database [conf]
  (map->Database
   {:conf (merge
           {:db-def db-def}
           conf)}))


(comment
  (use 'us.edwardstx.test.db :reload)

  (in-ns 'us.edwardstx.test.db)

  (defonce system (atom {}))

  (def c {:services {"auth.edwardstx.us" nil
                     "conf.edwardstx.us" nil
                     "hvac_daemon"       nil}
          :user-count 3})

  (reset! system  (component/system-map
                   :db (new-test-database c)))

  (swap! system component/start)
  (def db (:db @system))
  )
