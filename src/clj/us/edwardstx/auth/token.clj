(ns us.edwardstx.auth.token
  (:require [clj-time.core :as time]
            [us.edwardstx.auth.keys :as k]))

(defn extend-claims [conf claims]
  (let [n (time/now)]
    (assoc claims
           :iss (:service-name (:conf conf))
           :exp (time/plus n (time/days 1))
           :iat n)))

(defn issue-user-token [keys conf user sid]
  (k/sign keys
          (extend-claims conf
                         {:sub user
                          :jti sid})))

(defn issue-service-token [keys conf ksr]
  (k/sign keys
          (extend-claims conf ksr)))


