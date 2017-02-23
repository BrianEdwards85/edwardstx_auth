(ns us.edwardstx.common.jwt
  (:require [clojure.data.json :as json]
;;            [us.edwardstx.conf.client :as conf]
            [config.core :refer [env]]
            [buddy.sign.jwt :as jwt]
            [clj-crypto.core :as crypto]
            [clj-http.client :as client]))

(declare ^:dynamic *jwt*)

(def pubkey (crypto/decode-public-key
                {:algorithm "ECDSA" :bytes (-> env :public-key crypto/decode-base64)}))

(def header {:alg :es256})

(defn unsign [token]
    (try
      (jwt/unsign token pubkey header)
      (catch Exception e
        (println (.getMessage e))
        nil)))

(defn wrap-jwt [handler]
  (fn [request]
    (if-let [t (get-in request [:cookies "uid" :value])]
      (if-let [v (unsign t)]
        (binding [*jwt* v]
          (handler (assoc request :jwt *jwt*)))
        (binding [*jwt* nil] (handler request)))
      (binding [*jwt* nil] (handler request)))))
