(ns us.edwardstx.test.keys
  (:require [clj-crypto.core :as crypto]))

(defn create-key-pair []
  (let [key-pair (crypto/generate-key-pair :key-size 256 :algorithm "ECDSA")
        key-pair-map (crypto/get-key-pair-map key-pair)]
    {:key-pair key-pair
     :public-key (crypto/public-key key-pair)
     :private-key (crypto/private-key key-pair)
     :key-pair-map key-pair-map
     :public-key-base64 (-> key-pair-map :public-key :bytes crypto/encode-base64-as-str)
     :private-key-base64 (-> key-pair-map :private-key :bytes crypto/encode-base64-as-str)}))

