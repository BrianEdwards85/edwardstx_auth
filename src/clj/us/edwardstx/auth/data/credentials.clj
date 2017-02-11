(ns us.edwardstx.auth.data.credentials
  (:require [us.edwardstx.auth.data.db :refer [ds]]
            [yesql.core :refer [defqueries]]))

(defqueries "sql/credentials.sql"
  {:connection {:datasource  ds}})

(defn get-credentials [user]
  (first (get-credentials-sql {:email user})))

(defn set-credentials! [user salt hash]
  (set-credentials-sql! {:email user :hash hash :salt salt}))
