(ns us.edwardstx.auth.keys
  (:require [clj-crypto.core :as crypto]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [com.stuartsierra.component :as component]
            [us.edwardstx.common.spec :as specs]
            [clojure.spec :as s]))

(def headder {:alg :es256})
(def ec-cipher (crypto/create-cipher "ECIES" ))

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

(defn new-keys [env]
  {:pre [(s/valid? ::public-private-keys env)]}
  (map->Keys {:env (select-keys env [:public-key :private-key])}))

(defn sign [keys claims]
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

