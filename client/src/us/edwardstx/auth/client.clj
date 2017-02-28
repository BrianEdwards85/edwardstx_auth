(ns us.edwardstx.auth.client
  (:require [clojure.data.json :as json]
            [clj-crypto.core :as crypto]
            [byte-streams :as bs]
            [overtone.at-at :as atat]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [clj-time.core :as time]
            [clj-time.coerce :as ctime]
            [buddy.sign.jwt :as jwt])
  (:import java.util.UUID))

(declare ^:dynamic *jwt*)
(defn uuid [] (str (UUID/randomUUID)))
(def header {:alg :es256})
(defonce exp-interval (atom (time/minutes 1)))

(defn generate-key-sign-request [service]
  {:sub service
   :iss service
   :jti (uuid)
   :exp (time/plus (time/now) @exp-interval)})

(defn read-private-key [key]
  (if (string? key)
    (->> key
         crypto/decode-base64
         (assoc {:algorithm "ECDSA"} :bytes)
         crypto/decode-private-key)
    (crypto/as-private-key key)))

(defn generate-key-sign-request-token [service key]
  (jwt/sign (generate-key-sign-request service) key header))

(defn get-auth-key []
  (d/chain
   (http/get "https://auth.edwardstx.us/key")
   :body
   bs/to-string
   crypto/decode-base64
   #(assoc {:algorithm "ECDSA"} :bytes %)
   crypto/decode-public-key))

(defn get-service-token [service key]
    (d/chain
     (http/post (str "https://auth.edwardstx.us/service/" service "/token")
                {:body (generate-key-sign-request-token service key)})
     :body
     bs/to-string))

(defn refresh-token [a auth]
  (let [pool (atat/mk-pool)
        {:keys [token auth-key private-key]} auth
        {:keys [iat exp sub] :as claims} (jwt/unsign token auth-key header)
        ttl (- exp iat)
        check-int (quot ttl 15)
        bound (quot ttl 2)
        p {:ttl ttl :check-int check-int :bound bound :pool pool}
        service-token-fn #((if (> bound (- exp (ctime/to-epoch (time/now))))
                             (d/chain
                              (get-service-token sub private-key)
                              (fn [tk] (swap! a (fn [m] (assoc m :token tk)))))))]
    (reset! a (merge p auth))
    (atat/every check-int service-token-fn pool)
    auth))

(defn init-auth
  ([service key a]
   (d/chain
    (init-auth service key)
    #(refresh-token a %)
    ))
  ([service key]
   (let [private-key (read-private-key key)
         auth-key (get-auth-key)
         service-token (get-service-token service private-key)]
     (d/chain
      (d/zip auth-key service-token)
      (fn [r] {:private-key private-key :auth-key (first r) :token (second r)})))))

(defn unsign [token auth-atom]
    (try (jwt/unsign token (:auth-key @auth-atom) header)
      (catch Exception e
        (println (.getMessage e))
        nil)))

;;(defn wrap-jwt [handler]
;;  (fn [request]
;;    (if-let [t (get-in request [:cookies "uid" :value])]
;;      (if-let [v (unsign t)]
;;        (binding [*jwt* v]
;;          (handler (assoc request :jwt *jwt*)))
;;        (binding [*jwt* nil] (handler request)))
;;      (binding [*jwt* nil] (handler request)))))



;;(defn get-conf-impl []
;;  (let [secret (:conf-secret env)
;;        m (md5 secret)
;;        s (:service-name env)
;;        headers {:headers {:keyhash m}}]
;;    (-> (str (:conf-host env) s)
;;        (http/get headers)
;;        deref
;;        :body
;;        bs/to-string
;;        (crypt/decrypt-from-base64 secret)
;;        (json/read-str :key-fn keyword))))


