(ns us.edwardstx.auth.keys
  (:require [clj-crypto.core :as crypto]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [com.stuartsierra.component :as component]
            [us.edwardstx.common.spec :as specs]
            [clojure.spec.alpha :as s]))

(def headder {:alg :es256})
(def ec-cipher (crypto/create-cipher "ECIES" ))
(def exp-interval (atom (time/days 1)))

(defrecord Keys [key-pair env]
  component/Lifecycle

  (start [this]
    (assoc this :key-pair
           (crypto/decode-key-pair
            {:public-key {:algorithm "ECDSA"
                          :bytes (-> env :public-key crypto/decode-base64)}
             :private-key {:algorithm "ECDSA"
                           :bytes (-> env :private-key crypto/decode-base64)}})))

  (stop [this]
    (assoc this :key-pair nil)))

(s/def ::public-key ::specs/base64)
(s/def ::private-key ::specs/base64)
(s/def ::public-private-keys (s/keys :req-un [::public-key ::private-key]))
(s/def ::key-pair #(instance? java.security.KeyPair  %))
(s/def ::keys-record (s/keys :req-un [::key-pair]))


(defn new-keys [env]
  {:pre [(s/valid? ::public-private-keys env)]}
  (map->Keys {:env (select-keys env [:public-key :private-key :service-name])}))

(defn sign [keys claims]
  {:pre [(s/valid? ::keys-record keys)]}
      (jwt/sign claims
                (-> keys :key-pair crypto/private-key)
                headder))

(defn unsign [keys token]
  (try
    (jwt/unsign token (-> keys :key-pair crypto/public-key) headder)
    (catch Exception e
      nil)))

(defn decrypt [keys data]
  (crypto/decrypt
   (:key-pair keys)
   (crypto/decode-base64 data)
   ec-cipher))

(defn extend-claims [keys claims]
  (let [n (time/now)]
    (assoc claims
           :iss (-> keys :env :service-name)
           :exp (time/plus n @exp-interval)
           :iat n)))

(defn creat-claims
  ([sub]
   (creat-claims sub (str (java.util.UUID/randomUUID))))
  ([sub jti]
   {:sub sub :jti jti}))
