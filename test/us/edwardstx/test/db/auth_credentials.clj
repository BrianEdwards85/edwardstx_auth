(ns us.edwardstx.test.db.auth-credentials
  (:require [yesql.core :refer [defqueries]]
            [us.edwardstx.auth.authentication-test-data :refer [test-password]]))

(defqueries "sql/auth_credentials.sql")

(defn add-credential [db credential-map]
  (insert-credential<! (select-keys credential-map [:email :hash :secret :salt]) db))

(defn create-credential [db]
  (let [credential-map (test-password)]
    (add-credential db credential-map)
    credential-map))

(defn create-credentials [db conf]
  (assoc conf :users
         (doall
          (repeatedly (:user-count conf) #(create-credential db)))))

