(ns us.edwardstx.auth.authentication
  (:require [buddy.core.mac :as mac]
            [buddy.core.codecs :as codecs]
            [one-time.core :as ot]
            [us.edwardstx.auth.data.credentials :as c]
            [clojure.string :as s]
            [clojure.java.io :as io]
            ))

(def salt
  (delay
   (-> "salt.txt"
       io/resource
       io/file
       slurp
       s/trim)))

(defn verify-password [pw h]
  (mac/verify pw
              (codecs/hex->bytes h)
              {:key @salt :alg :hmac+sha256}))

(defn authenticate
  ([{:keys [user pass auth]}]
   (authenticate user pass auth))
  ([user pass auth]
   (if-let [{:keys [email, hash, secret]} (c/get-credentials user)]
     (and
      (verify-password pass hash)
      (ot/is-valid-totp-token? auth secret))
     false)))
