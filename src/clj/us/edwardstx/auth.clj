(ns us.edwardstx.auth
  (:require [config.core :refer [env]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [us.edwardstx.auth.core :refer [*semaphore*]]
            [us.edwardstx.auth.data.db :refer [new-database]]
            [us.edwardstx.auth.keys :refer [new-keys]]
            [us.edwardstx.auth.orchestrator :refer [new-orchestrator]]
            [us.edwardstx.auth.handler :refer [new-handler]]
            [us.edwardstx.auth.server :refer [new-server]]
            [us.edwardstx.auth.conf :refer [new-conf]])
(:gen-class) )


(defonce system (atom {}))

(defn init-system [env]
  (component/system-map
   :keys (new-keys env)
   :conf (component/using
          (new-conf env)
          [:keys])
   :db (component/using
        (new-database)
        [:conf])
   :handler (component/using
             (new-handler)
             [:keys :orchestrator])
   :orchestrator (component/using
                  (new-orchestrator)
                  [:db :keys])
   :server (component/using
            (new-server)
            [:handler :conf])
   ))

(defn -main [& args]
  (binding [*semaphore* (d/deferred)]
    (reset! system (init-system env))

    (swap! system component/start)
    (deref *semaphore*)
    (component/stop @system)
    (shutdown-agents)))


(comment
  (use 'us.edwardstx.auth :reload)


  (in-ns 'us.edwardstx.auth)

  (reset! system (component/system-map :keys (new-keys env)))

  (swap! system component/start)

  (use 'midje.repl)

  (autotest)


  )
