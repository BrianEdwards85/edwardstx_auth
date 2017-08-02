(ns us.edwardstx.auth.handler
  (:require [compojure.core :refer [GET PUT DELETE POST routes]]
            [compojure.route :refer [not-found resources]]
            [us.edwardstx.auth.middleware :refer [wrap-middleware]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [manifold.stream :as ms]
            [clojure.data.json :as json]))

(defn root-redirect [r]
  "/auth/")

(defn app-routes []
  (routes
   (GET "/" [] root-redirect)
   (resources "/auth/assets/")
   (not-found "Not Found")
   ))

(defrecord Handler [http-handler semaphore]
  component/Lifecycle

  (start [this]
    (->> (app-routes)
         wrap-middleware
         (assoc this :http-handler)))

  (stop [this]
    (assoc this :http-handler nil))
  )

(defn new-handler []
  (map->Handler {:semaphore (d/deferred)}))
