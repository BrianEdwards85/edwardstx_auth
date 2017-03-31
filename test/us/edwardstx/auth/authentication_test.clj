(ns us.edwardstx.auth.authentication-test
  (:use midje.sweet)
  (:require [us.edwardstx.auth.authentication :as auth]
            [us.edwardstx.auth.authentication-test-data :as test-data]
            [us.edwardstx.auth.keys-test-data :as keys-test-data]
            [manifold.deferred :as d]
            ))

(let [{:keys [password salt hash]} (test-data/test-password)
       passwd-map (test-data/test-password)
       password1 (:password passwd-map)
       salt1 (:salt passwd-map)
       hash1 (:hash passwd-map)]

  (fact "verify-password functionality"
        (auth/verify-password password hash salt) => true
        (auth/verify-password (str "X" password) hash salt) => false
        (auth/verify-password nil hash salt) => (throws java.lang.AssertionError)
        (auth/verify-password "" hash salt) => (throws java.lang.AssertionError)
        (auth/verify-password password nil salt) => (throws java.lang.AssertionError)
        (auth/verify-password password "" salt) => (throws java.lang.AssertionError)
        (auth/verify-password password "#" salt) => (throws java.lang.AssertionError)
        (auth/verify-password password hash nil) => (throws java.lang.AssertionError)
        (auth/verify-password password hash "") => (throws java.lang.AssertionError)
        (auth/verify-password password hash "abef") => (throws java.lang.AssertionError))

  (facts
   (prerequisites
    (us.edwardstx.auth.data.credentials/get-credentials ..db.. ..email..) => (d/success-deferred  {:email ..email..
                                                                                                   :hash hash
                                                                                                   :salt salt
                                                                                                   :secret ..secret..})
    (us.edwardstx.auth.data.credentials/get-credentials ..db.. ..email2..) => (d/success-deferred  nil)
    (one-time.core/is-valid-totp-token? ..auth.. ..secret..) => true
    (one-time.core/is-valid-totp-token? ..authbad.. ..secret..) => false
    (clojure.spec/valid? anything anything) => true
    (us.edwardstx.common.uuid/uuid) => salt1)

   (fact "authenticate functionality"
         @(auth/authenticate ..db.. ..email.. password ..auth..) => true
         @(auth/authenticate ..db.. ..email.. password ..authbad..) => false
         @(auth/authenticate ..db.. ..email.. (str "X" password) ..auth..) => false
         @(auth/authenticate ..db.. ..email2.. password ..auth..) => false)

   (fact "update-password!"
         @(auth/update-password! ..db.. ..email.. password1) => 1
         (provided
          (us.edwardstx.auth.data.credentials/set-credentials! ..db.. ..email.. salt1 hash1) => (d/success-deferred 1)))

   (fact "authenticate-update-password!"
         @(auth/authenticate-update-password! ..db.. ..email.. password password1 ..auth..) => 1
         (provided
          (us.edwardstx.auth.data.credentials/set-credentials! ..db.. ..email.. salt1 hash1) => (d/success-deferred 1)))))

(let [{:keys [public-key-base64 public-key] :as key-pair} (keys-test-data/create-key-pair)]
  (fact "read-public-key"
        (auth/read-public-key public-key-base64) => public-key
        (auth/read-public-key nil) => (throws java.lang.AssertionError)))

(let [claims {:sub "sub_" :iss "sub_"}
      {:keys [public-key public-key-base64 signed]} (keys-test-data/init-test-data claims)
      key-map {:key public-key :key-str public-key-base64}
      expected-unsinged (assoc claims :key public-key-base64)]

  (fact "verify-service"
        (auth/verify-service (:sub claims) claims) => claims
        (auth/verify-service (str "not_" (:sub claims)) claims) => (throws Exception))

  (fact "unsign-ksr"
        (auth/unsign-ksr signed key-map) => expected-unsinged
        (auth/unsign-ksr (str "AS" signed "QA") key-map) => (throws Exception))

  (facts
   (prerequisites
    (auth/verify-service ..service.. expected-unsinged) => expected-unsinged
    (us.edwardstx.auth.data.services/get-service-key ..db.. ..service..) => (d/success-deferred public-key-base64))
   (fact "validate-token"
         @(auth/validate-token ..db.. ..service.. signed) => expected-unsinged
         @(auth/validate-token ..db.. ..service.. (str "ErE" signed "tears")) => (throws Exception))))
