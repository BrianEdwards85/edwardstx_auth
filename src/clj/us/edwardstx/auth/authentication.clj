(ns us.edwardstx.auth.authentication
  (:require [buddy.core.mac :as mac]
            [buddy.core.codecs :as codecs]
            [buddy.sign.jwt :as jwt]
            [clj-crypto.core :as crypto]
            [one-time.core :as ot]
            [manifold.deferred :as d]
            [clojure.spec :as s]
            [us.edwardstx.auth.data.credentials :as cred]
            [us.edwardstx.auth.data.services :as services]
            [us.edwardstx.common.spec :as specs]
            [us.edwardstx.common.uuid :refer [uuid]]))

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

(defn verify-auth-and-password [pass auth cred-map]
  (if (nil? cred-map)
    false
    (let [{:keys [email hash salt secret]} cred-map]
      (and
       (verify-password pass hash salt)
       (ot/is-valid-totp-token? auth secret)))))

(defn authenticate
  ([db {:keys [user pass auth]}]
   (authenticate db user pass auth))
  ([db user pass auth]
   {:pre [(s/valid? ::user user)
          (s/valid? ::pass pass)
          (s/valid? ::auth auth)]}
   (d/chain (cred/get-credentials db user) #(verify-auth-and-password pass auth %))))

(defn update-password! [db user pass]
  {:pre [(s/valid? ::user user)
         (s/valid? ::pass pass)]}
  (let [salt (uuid)]
    (cred/set-credentials! db
                           user
                           salt
                           (-> (mac/hash pass {:key salt :alg :hmac+sha256})
                               (codecs/bytes->hex)))))

(defn authenticate-update-password!
  ([db {:keys [user opass npass auth]}]
   (authenticate-update-password! db user opass npass auth))
  ([db user opass npass auth]
   {:pre [(s/valid? ::user user)
          (s/valid? ::pass opass)
          (s/valid? ::pass npass)
          (s/valid? ::auth auth)]}
   (d/chain (authenticate db user opass auth)
            #(if % (update-password! db user npass) false))))

(defn read-public-key [key-string]
  {:pre [(s/valid? ::specs/base64 key-string)]}
  (->> key-string
       crypto/decode-base64
       (assoc {:algorithm "ECDSA"} :bytes)
       crypto/decode-public-key))

(defn unsign-ksr [ksr {:keys [key key-str] :as key-map}]
  (assoc
   (jwt/unsign ksr key {:alg :es256})
   :key key-str))

(defn validate-token [db service ksr]
  (d/chain
   (services/get-service-key db service)
   #(unsign-ksr ksr {:key (read-public-key %1) :key-str %1})))
