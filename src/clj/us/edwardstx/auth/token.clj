(ns us.edwardstx.auth.token
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [clj-time.core :as time]
            [clojure.string :as s]
            [us.edwardstx.auth.core :refer [conf]]))

(def ec-privkey (keys/str->private-key (s/replace (-> conf :jwt :private-key) "\\n" "\n")))
(def ec-pubkey (keys/str->public-key  (s/replace (-> conf :jwt :public-key) "\\n" "\n")))

(def headder {:alg :es256} ) ;;(-> conf :jwt :headder))
(def issuer (-> conf :jwt :iss))


;;(def myclaims {:sub "bedwards.cs.utsa.edu@gmail.com" :perm '("admin" "users")})

(defn extend-claims [claims]
  (assoc claims
         :iss issuer
         :exp (time/plus (time/now) (time/days 1))))

(defn sign [claims]
  (jwt/sign (extend-claims claims) ec-privkey headder))


(defn unsign [token]
  (try
    (jwt/unsign token ec-pubkey headder)
    (catch Exception e
      (println (.getMessage e))
      nil)))

(defn issue-token [user s]
  (sign {:sub user :jti s}))





