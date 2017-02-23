(ns us.edwardstx.auth
  (:require [us.edwardstx.auth.handler :refer [app semaphore]]
            [us.edwardstx.auth.middleware :refer [wrap-middleware]]
            [us.edwardstx.conf.client :refer [get-conf]]
            [aleph.http :as http]
            [clojure.tools.nrepl.server :as nrepl])
  (:gen-class))

(defonce http-server-atom (atom nil))
(defonce nrepl-server-atom (atom nil))

(defn create-server [p]
  (http/start-server app {:port p}))

(defn start-or-restart-server [p]
  (swap! http-server-atom (fn [old-server]
                            (when old-server (.close old-server))
                            (create-server p))))

(defn stop-server []
  (swap! http-server-atom (fn [old-server]
                            (when old-server (.close old-server))
                            nil)))

(defn -main [& args]
  (let [conf (get-conf)
        http-port (Integer/parseInt (:http-port conf))
        nrepl-port (Integer/parseInt (:nrepl-port conf))]
    (start-or-restart-server http-port)
    (reset! nrepl-server-atom (nrepl/start-server :port nrepl-port))
    @semaphore
    (stop-server)
    (nrepl/stop-server @nrepl-server-atom))
  )

