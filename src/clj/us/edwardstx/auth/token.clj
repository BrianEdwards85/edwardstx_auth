(ns us.edwardstx.auth.token
  (:require [buddy.sign.jwt :as jwt]
            [clj-crypto.core :as crypto]
            [us.edwardstx.auth.data.services :as data]
            [config.core :refer [env]]
            [clj-time.core :as time]
            [clojure.string :as s]
            [us.edwardstx.auth.core :refer [conf]]))

(def key-pair
  (crypto/decode-key-pair
   {:public-key {:algorithm "ECDSA"
                 :bytes (-> env :public-key crypto/decode-base64)}
    :private-key {:algorithm "ECDSA"
                  :bytes (-> env :private-key crypto/decode-base64)}}))

(def ec-privkey (crypto/private-key key-pair))
(def ec-pubkey (crypto/public-key key-pair))

(def headder {:alg :es256})
(def issuer (-> conf :jwt :iss))

(def exp-interval (atom (time/days 1)))

(defn extend-claims [claims]
  (let [n (time/now)]
    (assoc claims
           :iss issuer
           :exp (time/plus n @exp-interval)
           :iat n)))


(defn sign [claims]
  (jwt/sign (extend-claims claims) ec-privkey headder))


(defn unsign
  ([token] (unsign token ec-pubkey))
  ([token key]
   (try
     (jwt/unsign token (crypto/as-public-key key) headder)
     (catch Exception e
       (println (.getMessage e))
       nil))))

(defn issue-token [user s]
  (sign {:sub user :jti s}))

(defn read-public-key [key-string]
    (->> key-string
         crypto/decode-base64
         (assoc {:algorithm "ECDSA"} :bytes)
         crypto/decode-public-key))

(defn issue-service-token [service ksr]
  (if-let [key-string (data/get-service-key service)]
    (if-let [service-public-key (read-public-key key-string)]
      (if-let [ksr-claimes (unsign ksr service-public-key)]
        (if (= service (:sub ksr-claimes))
          (jwt/sign
           (assoc (extend-claims ksr-claimes) :key key-string)
           ec-privkey
           headder)
          nil)
        nil)
      nil)
    nil))




