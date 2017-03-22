(ns us.edwardstx.auth.authentication-test-data
  (:require [us.edwardstx.common.spec :as specs]
            [buddy.core.mac :as mac]
            [buddy.core.codecs :as codecs]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(defn test-password []
  (let [password (gen/generate (s/gen ::specs/hex))
        salt (str (java.util.UUID/randomUUID))]
    {:password password
     :email (str (gen/generate (s/gen ::specs/hex)) "@testing.org")
     :salt salt
     :hash (codecs/bytes->hex (mac/hash password {:key salt :alg :hmac+sha256}))}))

