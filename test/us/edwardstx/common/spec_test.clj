(ns us.edwardstx.common.spec-test
  (:use midje.sweet)
  (:require [us.edwardstx.common.spec :as spec]
            [clojure.spec.alpha :as s]))

(fact "::non-empty-string validates non empty strings"
      (s/valid? ::spec/non-empty-string "Valid string") => true
      (s/valid? ::spec/non-empty-string "") => false
      (s/valid? ::spec/non-empty-string nil) => false
      (s/valid? ::spec/non-empty-string {}) => false)

(fact "::base64 validates non empty Base 64 encoded strings"
      (s/valid? ::spec/base64 "AbCd/EfG+") => true
      (s/valid? ::spec/base64 "=AbCd/EfG+") => false
      (s/valid? ::spec/base64 "AbCd/EfG+===") => false
      (s/valid? ::spec/base64 "AbCd/E==fG+") => false
      (s/valid? ::spec/base64 "AbC$?") => false
      (s/valid? ::spec/base64 "") => false
      (s/valid? ::spec/base64 nil) => false
      (s/valid? ::spec/base64 {}) => false)



