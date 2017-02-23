(ns us.edwardstx.auth.data.services
  (:require [us.edwardstx.auth.data.db :refer [ds]]
            [yesql.core :refer [defqueries]]))


(defqueries "sql/services.sql"
  {:connection {:datasource  ds}})

(defn get-service-key [service]
  (:public_key (first (get-service-key-sql {:service service}))))
