(ns us.edwardstx.auth.authentication-test
  (:use midje.sweet)
  (:require [us.edwardstx.auth.authentication :as auth]
            [us.edwardstx.auth.authentication-test-data :as test-data]
            ))

(defonce td (atom nil))

(against-background
 [(before :contents (reset! td (test-data/test-password)))]
 (fact "verify-password functionality"
       (auth/verify-password (:password @td) (:hash @td) (:salt @td)) => true
       (auth/verify-password (str "X" (:password @td)) (:hash @td) (:salt @td)) => false
       (auth/verify-password nil (:hash @td) (:salt @td)) => (throws java.lang.AssertionError)
       (auth/verify-password "" (:hash @td) (:salt @td)) => (throws java.lang.AssertionError)
       (auth/verify-password (:password @td) nil (:salt @td)) => (throws java.lang.AssertionError)
       (auth/verify-password (:password @td) "" (:salt @td)) => (throws java.lang.AssertionError)
       (auth/verify-password (:password @td) "#" (:salt @td)) => (throws java.lang.AssertionError)
       (auth/verify-password (:password @td) (:hash @td) nil) => (throws java.lang.AssertionError)
       (auth/verify-password (:password @td) (:hash @td) "") => (throws java.lang.AssertionError)
       (auth/verify-password (:password @td) (:hash @td) "abef") => (throws java.lang.AssertionError)))


(against-background
 [(before :contents (reset! td (test-data/test-password)))]
 (fact "authenticate valid credentials functionality"
       (auth/authenticate ..db.. ..email.. (:password @td) ..auth..) => true
       (provided
        (clojure.spec/valid? anything anything) => true
        (us.edwardstx.auth.data.credentials/get-credentials ..db.. ..email..) => {:email ..email..
                                                                                  :hash (:hash @td)
                                                                                  :salt (:salt @td)
                                                                                  :secret ..secret..}
        (one-time.core/is-valid-totp-token? ..auth.. ..secret..) => true))
 (fact "authenticate invalid credentials functionality"
       (auth/authenticate ..db.. ..email.. (:password @td) ..auth..) => false
       (provided
        (clojure.spec/valid? anything anything) => true
        (us.edwardstx.auth.data.credentials/get-credentials ..db.. ..email..) => {:email ..email..
                                                                                  :hash (:hash @td)
                                                                                  :salt (:salt @td)
                                                                                  :secret ..secret..}
        (one-time.core/is-valid-totp-token? ..auth.. ..secret..) => false))
 (fact "authenticate invalid credentials functionality"
       (auth/authenticate ..db.. ..email.. (str "X" (:password @td)) ..auth..) => false
       (provided
        (clojure.spec/valid? anything anything) => true
        (us.edwardstx.auth.data.credentials/get-credentials ..db.. ..email..) => {:email ..email..
                                                                                  :hash (:hash @td)
                                                                                  :salt (:salt @td)
                                                                                  :secret ..secret..}))
 (fact "authenticate missing credentials functionality"
       (auth/authenticate ..db.. ..email.. (:password @td) ..auth..) => false
       (provided
        (clojure.spec/valid? anything anything) => true
        (us.edwardstx.auth.data.credentials/get-credentials ..db.. ..email..) => nil))

 )
