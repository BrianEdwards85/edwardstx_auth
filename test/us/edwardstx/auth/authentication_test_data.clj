(ns us.edwardstx.auth.authentication-test-data
  (:require [us.edwardstx.common.spec :as specs]
            [buddy.core.mac :as mac]
            [one-time.core :as ot]
            [buddy.core.codecs :as codecs]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defn test-password []
  (let [password (gen/generate (s/gen ::specs/hex))
        salt (str (java.util.UUID/randomUUID))]
    {:password password
     :email (str (gen/generate (s/gen ::specs/hex)) "@testing.org")
     :salt salt
     :secret (ot/generate-secret-key)
     :hash (codecs/bytes->hex (mac/hash password {:key salt :alg :hmac+sha256}))}))

