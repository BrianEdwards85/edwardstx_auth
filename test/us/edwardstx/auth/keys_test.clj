(ns us.edwardstx.auth.keys-test
  (:use midje.sweet)
  (:require [us.edwardstx.auth.keys :as keys]
            [buddy.sign.jwt :as jwt]
            [clj-crypto.core :as crypto]
            [us.edwardstx.auth.keys-test-data :refer [init-test-data]]
            [clojure.spec :as s]))

(defonce td (atom nil))

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

(against-background
 [(before :contents (reset! td (init-test-data)))]
 (fact "Encrypt/decrypt"
       (jwt/unsign
        (keys/sign (:keys @td) (:claims @td))
        (-> @td :key-pair crypto/public-key)
        keys/headder) => (:claims @td)

       (keys/unsign (:keys @td) (:signed @td)) => (:claims @td)

       (keys/decrypt (:keys @td) (:encrypted @td)) => (:claim-string  @td)))
