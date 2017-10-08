(ns us.edwardstx.auth.handler
  (:require [compojure.core :refer [GET PUT DELETE POST routes]]
            [compojure.route :refer [not-found resources]]
            [us.edwardstx.auth.middleware :refer [wrap-middleware]]
            [us.edwardstx.auth.orchestrator :as orchestrator]
            [us.edwardstx.auth.keys :as keys]
            [us.edwardstx.auth.html :refer [loading-page]]
            [us.edwardstx.auth.core :refer [*jwt*]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [manifold.stream :as ms]
            [clojure.data.json :as json]))

(def authentication-failed {:status 401 :body "Authentication Failed" :headers {"Content-Type" "text/plain"}})
(def invalid-token {:status 400 :body "Invalid Token" :headers {"Content-Type" "text/plain"}})
(def json-header {"Content-Type" "application/json"})

(defn root-redirect [r]
  {:status 302
   :headers {"Location" "/auth/"}
   :body    ""}
  )

(defn read-body [r]
  (->> r :body slurp))

(defn validate-post [r]
  (let [t (read-body r)]
    (if-let [v (keys/unsign (:keys r) t)]
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

(defn auth [r]
  (let [j (json/read-str (read-body r) :key-fn keyword)
        cred (if (string? (:auth j)) (assoc j :auth (read-string (:auth j))) j)]
    (-> (orchestrator/authenticate (:orchestrator r) cred)
        (d/chain #(hash-map
                   :status 200
                   :body (json/write-str (select-keys %1 [:token :jti :sub :iss]))
                   :headers json-header
                   :cookies {"uid" {:value (:token %1) :domain ".edwardstx.us" :max-age 86000 :path "/"}}))
        (d/catch (fn [e] authentication-failed))
        )))

(defn service-token [r]
  (let [service (get-in r [:route-params :service])
        ksr (read-body r)]
    (-> (orchestrator/service-token (:orchestrator r) service ksr)
        (d/chain #(hash-map
                   :status 200
                   :body %1))
        (d/catch (fn [e] authentication-failed)))))

(defn public-key [r]
  (-> r :keys :env :public-key))

(defn echo [r]
  (do {:body (json/write-str (dissoc  r :body :keys :orchestrator))
       :headers json-header
       :status 200}))

(defn wrap-handler [cmpt handler]
  (fn [r]
    (let [request (merge (select-keys cmpt [:keys :orchestrator]) r)]
      (if-let [t (get-in r [:cookies "uid" :value])]
        (if-let [v (keys/unsign (:keys request) t)]
          (binding [*jwt* v]
            (handler (assoc request :jwt v)))
              (binding [*jwt* nil] (handler request)))
            (binding [*jwt* nil] (handler request))))))

(defn app-routes []
  (routes
   (GET  "/" [] root-redirect)
   (GET  "/auth/" [] loading-page)
   (GET  "/auth/about" [] loading-page)
   (GET  "/auth/whoami" [] loading-page)
   (GET  "/auth/key" [] public-key)
   (POST "/auth/api/auth" [] auth)
   (POST "/auth/api/service/:service/token" [service] service-token)
   (POST "/auth/api/validate" [] validate-post)
   (GET  "/auth/api/validate" [] validate-get)
   (GET  "/auth/api/echo" [] echo)
   (resources "/auth/assets/")
   (not-found "Not Found")
   ))

(defrecord Handler [http-handler keys orchestrator]
  component/Lifecycle

  (start [this]
    (->> (app-routes)
         (wrap-handler this)
         wrap-middleware
         (assoc this :http-handler)))

  (stop [this]
    (assoc this :http-handler nil))
  )

(defn new-handler []
  (map->Handler {}))
