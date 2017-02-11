(ns us.edwardstx.auth.authentication
  (:require [buddy.core.mac :as mac]
            [buddy.core.codecs :as codecs]
            [one-time.core :as ot]
            [us.edwardstx.auth.data.credentials :as c]
            [us.edwardstx.common.uuid :refer [uuid]]))

(defn verify-password [pass hash salt]
  (mac/verify pass
              (codecs/hex->bytes hash)
              {:key salt :alg :hmac+sha256}))

(defn update-password! [user pass]
  (let [salt (uuid)]
    (c/set-credentials! user
                       salt
                       (-> (mac/hash pass {:key salt :alg :hmac+sha256})
                           (codecs/bytes->hex)))))

(defn authenticate
  ([{:keys [user pass auth]}]
   (authenticate user pass auth))
  ([user pass auth]
   (if-let [{:keys [email hash salt secret]} (c/get-credentials user)]
     (and
      (verify-password pass hash salt)
      (ot/is-valid-totp-token? auth secret))
     false)))

(defn authenticate-update-password!
  ([{:keys [user opass npass auth]}]
   (authenticate-update-password! user opass npass auth))
  ([user opass npass auth]
   (if (authenticate user opass auth)
     (do
       (update-password! user npass)
       true)
     false)))
