(ns us.edwardstx.auth.core
  (:require [us.edwardstx.conf.client :as c]
            [clj-crypto.core :as crypto]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [config.core :refer [env]]))


(declare ^:dynamic *jwt*)
(defonce service-key (atom nil))
(def exp-interval (atom (time/days 1)))
(def service-name (:service-name env))

(def key-pair
  (crypto/decode-key-pair
   {:public-key {:algorithm "ECDSA"
                 :bytes (-> env :public-key crypto/decode-base64)}
    :private-key {:algorithm "ECDSA"
                  :bytes (-> env :private-key crypto/decode-base64)}}))

(def private-key (crypto/private-key key-pair))
(def public-key (crypto/public-key key-pair))

(def headder {:alg :es256})
(def issuer (:service-name env))

(defn create-service-key []
  (jwt/sign {:iss issuer
             :sub issuer
             :exp (time/plus (time/now) @exp-interval)
             :jti (str (java.util.UUID/randomUUID))
             :key (:public-key env)}
            private-key
            headder))


;;(def conf {})

;;(def conf @(c/get-conf service-name (create-service-key) key-pair))
(def conf (c/get-conf))
