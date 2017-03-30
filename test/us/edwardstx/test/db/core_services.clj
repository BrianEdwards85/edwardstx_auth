(ns us.edwardstx.test.db.core-services
  (:require [yesql.core :refer [defqueries]]
            [us.edwardstx.test.keys :refer [create-key-pair]]))

(defqueries "sql/core_services.sql")

(defn add-service [db service public-key-base64]
  (insert-service<! {:service service :public_key public-key-base64} db))

(defn create-service [db service]
  (let [key-pair-map (create-key-pair)]
    (add-service db service (:public-key-base64 key-pair-map))
    key-pair-map))

(defn create-services [db conf]
  (assoc conf :services
         (->> (:services conf)
              keys
              (mapcat #(vector %1 (create-service db %1)))
              (apply hash-map))))
