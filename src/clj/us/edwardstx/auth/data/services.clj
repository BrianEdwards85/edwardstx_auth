(ns us.edwardstx.auth.data.services
  (:require [us.edwardstx.auth.data.db :refer [get-connection]]
          [manifold.deferred :as d]
          [yesql.core :refer [defqueries]]))

(defqueries "sql/services.sql")

(defn get-service-key [db service]
  (:public_key (first (get-service-key-sql {:service service} (get-connection db)))))
