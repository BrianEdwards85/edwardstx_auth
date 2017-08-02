(ns us.edwardstx.auth.keys-test
  (:use midje.sweet)
  (:require [us.edwardstx.auth.keys :as keys]
            [buddy.sign.jwt :as jwt]
            [clj-crypto.core :as crypto]
            [us.edwardstx.auth.keys-test-data :refer [init-test-data]]
            [clojure.spec.alpha :as s]))

(fact "::public-private-keys validates map of Base 64 encoded key pair"
      (s/valid? ::keys/public-private-keys {:public-key "Ab+Cd"  :private-key "EdeE=="}) => true
      (s/valid? ::keys/public-private-keys [:public-key "AbCd" :private-key "EdeE"]) => false
      (s/valid? ::keys/public-private-keys {:public-key "AbCd"}) => false
      (s/valid? ::keys/public-private-keys {:public-key "AbCd"  :private-key 1}) => false
      (s/valid? ::keys/public-private-keys {:public-key "AbCd" :private-key nil}) => false
      (s/valid? ::keys/public-private-keys {:public-key "AbCd" :private-key "?"}) => false)

(fact "new-keys creates new Keys with valid key pair map"
      (keys/new-keys {}) => (throws java.lang.AssertionError)
      (keys/new-keys {:public-key "Ab+Cd"  :private-key "EdeE=="}) => anything)

(let [{:keys [keys
              key-pair
              claims
              signed
              encrypted
              claim-string]} (init-test-data {:iss "a.b.c" :sub "test"})]

  (fact "Encrypt/decrypt"
        (jwt/unsign (keys/sign keys claims) (crypto/public-key key-pair) keys/headder) => claims

        (keys/unsign keys signed) => claims

        (keys/decrypt keys encrypted) => claim-string))
