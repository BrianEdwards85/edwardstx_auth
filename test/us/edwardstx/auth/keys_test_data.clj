(ns us.edwardstx.auth.keys-test-data
  (:require [clj-crypto.core :as crypto]
            [buddy.sign.jwt :as jwt]
            [us.edwardstx.auth.keys :as keys]
            [com.stuartsierra.component :as component]
            [clojure.data.json :as json]
            ))

(def headder {:alg :es256})
(def ec-cipher (crypto/create-cipher "ECIES" ))

(defn start-keys [env]
  (component/start
   (component/system-map
    :keys (keys/new-keys env))))

(defn init-test-data []
  (let [key-pair (crypto/generate-key-pair :key-size 256 :algorithm "ECDSA")
        key-pair-map (crypto/get-key-pair-map key-pair)
        public-key-base64 (-> key-pair-map :public-key :bytes crypto/encode-base64-as-str)
        private-key-base64 (-> key-pair-map :private-key :bytes crypto/encode-base64-as-str)
        env {:public-key public-key-base64 :private-key private-key-base64}
        claims {:iss "a.b.c" :sub "test"}
        claim-string (json/write-str claims)
        signed (jwt/sign claims (crypto/private-key key-pair) headder)
        encrypted (crypto/encode-base64-as-str (crypto/encrypt key-pair claim-string ec-cipher))
        sys (start-keys env)
        keys (:keys sys)]
    {:key-pair key-pair
     :key-pair-map key-pair-map
     :public-key-base64 public-key-base64
     :private-key-base64 private-key-base64
     :env env
     :claims claims
     :claim-string claim-string
     :signed signed
     :encrypted encrypted
     :keys keys}))

