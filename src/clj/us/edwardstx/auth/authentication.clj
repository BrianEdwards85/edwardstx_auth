(ns us.edwardstx.auth.authentication
  (:require [buddy.core.mac :as mac]
            [one-time.core :as ot]
            [us.edwardstx.auth.data.credentials :as cred]
            [manifold.deferred :as d]
            [clojure.spec :as s]
            [us.edwardstx.common.spec :as specs]
            [us.edwardstx.common.uuid :refer [uuid]]
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

(comment
  (if-let [{:keys [email hash salt secret]} (cred/get-credentials db user)]
    (and
     (verify-password pass hash salt)
     (ot/is-valid-totp-token? auth secret))
    false))

(defn update-password! [db user pass]
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
   (d/chain (authenticate db user opass auth)
            #(if % (update-password! db user npass) false))))

(comment
  (if (authenticate db user opass auth)
    (do
      (update-password! db user npass)
      true)
    false))
