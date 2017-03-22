(ns us.edwardstx.auth.authentication
  (:require [buddy.core.mac :as mac]
            [one-time.core :as ot]
            [us.edwardstx.auth.data.credentials :as cred]
            [clojure.spec :as s]
            [us.edwardstx.common.spec :as specs]
            [buddy.core.codecs :as codecs]))


(s/def ::user ::specs/non-empty-string)
(s/def ::pass ::specs/non-empty-string)
(s/def ::hash ::specs/hex)
(s/def ::salt ::specs/uuid)
(s/def ::auth ::specs/positive-int)

(defn verify-password [pass hash salt]
  {:pre [(s/valid? ::pass pass)
         (s/valid? ::hash hash)
         (s/valid? ::salt salt)]}
  (mac/verify pass
              (codecs/hex->bytes hash)
              {:key salt :alg :hmac+sha256}))

(defn authenticate
  ([db {:keys [user pass auth]}]
   (authenticate db user pass auth))
  ([db user pass auth]
   {:pre [(s/valid? ::user user)
          (s/valid? ::pass pass)
          (s/valid? ::auth auth)]}
   (if-let [{:keys [email hash salt secret]} (cred/get-credentials db user)]
     (and
      (verify-password pass hash salt)
      (ot/is-valid-totp-token? auth secret))
     false)))

