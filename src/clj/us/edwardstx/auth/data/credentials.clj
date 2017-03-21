(ns us.edwardstx.auth.data.credentials
  (:require [us.edwardstx.auth.data.db :refer [get-connection]]
            [manifold.deferred :as d]
            [yesql.core :refer [defqueries]]))

(defqueries "sql/credentials.sql")

(defn get-credentials [db user]
  (d/future
    (first (get-credentials-sql {:email user} (get-connection db)))))

(defn set-credentials! [db user salt hash]
  (d/future
    (set-credentials-sql! {:email user :hash hash :salt salt} (get-connection db))))

