(ns us.edwardstx.common.jwt
  (:require [clojure.data.json :as json]
            [us.edwardstx.conf.client :as conf]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [clj-http.client :as client]))

(declare ^:dynamic *jwt*)


;;(defn get-key-map []
;;  (-> "https://auth.edwardstx.us/key"
;;      client/get
;;      (json/read-str :key-fn keyword)))

(defn get-key-map []
  {:key (-> (conf/get-conf) :jwt :public-key)
   :header {:alg :es256}})

(def jwt-keys 
  (delay
   (let [{:keys [key] :as key-map} (get-key-map)
         pubkey (keys/str->public-key key)]
     (assoc key-map :pubkey pubkey ))))


(defn unsign [token]
  (let [{:keys [pubkey header]} @jwt-keys]
    (try
      (jwt/unsign token pubkey header)
      (catch Exception e
        (println (.getMessage e))
        nil))))

(defn wrap-jwt [handler]
  (fn [request]
    (if-let [t (get-in request [:cookies "uid" :value])]
      (if-let [v (unsign t)]
        (binding [*jwt* v]
          (handler (assoc request :jwt *jwt*)))
        (binding [*jwt* nil] (handler request)))
      (binding [*jwt* nil] (handler request)))))
