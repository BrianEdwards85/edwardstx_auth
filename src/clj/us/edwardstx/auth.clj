(ns us.edwardstx.auth
  (:require [config.core :refer [env]]
            [com.stuartsierra.component :as component]
            [us.edwardstx.auth.data.db :refer [new-database]]
            [us.edwardstx.auth.keys :refer [new-keys]]
            [us.edwardstx.auth.conf :refer [new-conf]]))


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

   ))

(defn -main [&args]
  (reset! system (init-system env))
  (swap! system component/start)

  )

