(ns us.edwardstx.auth.token
  (:require [buddy.sign.jwt :as jwt]
            [clj-crypto.core :as crypto]
            [us.edwardstx.auth.data.services :as data]
            [clj-time.core :as time]
            [us.edwardstx.auth.core :as core :refer [headder issuer exp-interval]]))

(defn extend-claims [claims]
  (let [n (time/now)]
    (assoc claims
           :iss issuer
           :exp (time/plus n @exp-interval)
           :iat n)))

(defn sign [claims key]
  (jwt/sign (extend-claims claims) (crypto/as-private-key key) headder))

(defn unsign [token key]
   (try
     (jwt/unsign token (crypto/as-public-key key) headder)
     (catch Exception e
       (println (.getMessage e))
       nil)))

(defn issue-token [user s key]
  (sign {:sub user :jti s} key))

(defn read-public-key [key-string]
    (->> key-string
         crypto/decode-base64
         (assoc {:algorithm "ECDSA"} :bytes)
         crypto/decode-public-key))

(defn issue-service-token [service ksr key]
  (if-let [key-string (data/get-service-key service)]
    (if-let [service-public-key (read-public-key key-string)]
      (if-let [ksr-claimes (unsign ksr service-public-key)]
        (if (= service (:sub ksr-claimes))
          (jwt/sign
           (assoc (extend-claims ksr-claimes) :key key-string)
           (crypto/as-private-key key)
           headder)
          nil)
        nil)
      nil)
    nil))




