(ns us.edwardstx.auth.data.db
  (:require [hikari-cp.core :refer :all]
            [us.edwardstx.auth.core :refer [conf]]))

(def ds-options
  (let [{:keys [subname subprotocol] :as db-conf} (:db  conf)]
    (assoc
     (into {} 
           (filter
            #(contains?
              #{:username :password :jdbc-url}
              (first %))
            db-conf))
     :jdbc-url (str "jdbc:" subprotocol ":" subname))))

(def ds (make-datasource ds-options))
