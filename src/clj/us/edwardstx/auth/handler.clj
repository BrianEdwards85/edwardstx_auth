(ns us.edwardstx.auth.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [us.edwardstx.auth.middleware :refer [wrap-middleware]]
            [clojure.data.json :as json]
            [manifold.deferred :as d]
            [config.core :refer [env]]
            [us.edwardstx.common.uuid :refer [uuid]]
            [us.edwardstx.auth.html :refer [loading-page]]
            [us.edwardstx.auth.core :refer [conf *jwt* key-pair]]
            [us.edwardstx.auth.authentication :as authentication]
            [us.edwardstx.auth.token :as token]))

(def semaphore (d/deferred))

(def authentication-failed {:status 401 :body "Authentication Failed" :headers {"Content-Type" "text/plain"}})
(def invalid-token {:status 400 :body "Invalid Token" :headers {"Content-Type" "text/plain"}})

(def json-header {"Content-Type" "application/json"})

(defn read-body [r]
  (->> r :body slurp))

(defn auth [r]
  (let [s (read-body r)
        j (json/read-str s :key-fn keyword)
        cred (if (string? (:auth j)) (assoc j :auth (read-string (:auth j))) j)]
    (if (authentication/authenticate cred)
      (let [sid (uuid)
            t (token/issue-token (:user cred) sid key-pair)]
        {:status 200
         :body (json/write-str {:sub (:user cred) :jti sid :iss "auth.edwardstx.us" :token t})
         :headers json-header
         :cookies {"uid" {:value t :domain ".edwardstx.us" :max-age 86000}}})
      authentication-failed)))

(defn validate-post [r]
  (let [t (read-body r)]
    (if-let [v (token/unsign t key-pair)]
      {:status 200
       :body (json/write-str v)
       :headers json-header}
      invalid-token)))

(defn validate-get [r]
  (if-let [v (:jwt r)]
    {:status 200
     :body (json/write-str v)
     :headers json-header}
    invalid-token))

(defn echo [r]
  (do {:body (json/write-str (dissoc  (assoc  r :env env) :body))
       :headers json-header
       :status 200}))

(defn logout [r]
  {:status 200
   :body (loading-page)
   :headers { "Content-Type" "text/html"}
   :cookies {"uid" {:value "_" :domain ".edwardstx.us" :max-age 1}}})

(defn service-token [r]
  (let [service (get-in r [:route-params :service])
        ksr (read-body r)]
    (if-let [token (token/issue-service-token service ksr key-pair)]
      {:status 200
       :body token
       :cookies {"service-token" {:value token :domain ".edwardstx.us" :max-age 86000}}}
      invalid-token)))

(defroutes routes
  (GET  "/" [] (loading-page))
  (GET  "/whoami" [] (loading-page))
  (GET  "/about" [] (loading-page))
  (GET  "/key" [] (:public-key env))
  (GET  "/logout" [] logout)

  (POST "/api/auth" [] auth)
  (POST "/api/service/:service/token" [service] service-token)
  (POST "/api/validate" [] validate-post)
  (GET  "/api/validate" [] validate-get)
  (GET  "/api/echo" [] echo)
  (resources "/")
  (not-found "Not Found"))

(defn wrap-jwt [handler]
  (fn [request]
    (if-let [t (get-in request [:cookies "uid" :value])]
      (if-let [v (token/unsign t key-pair)]
        (binding [*jwt* v]
          (handler (assoc request :jwt *jwt*)))
        (binding [*jwt* nil] (handler request)))
      (binding [*jwt* nil] (handler request)))))

(def app (wrap-middleware (wrap-jwt #'routes)))

(comment 
  (defn pub-key []
    (let [key (:public-key env)
          header token/headder]
      (json/write-str {:key key :header header}))))
