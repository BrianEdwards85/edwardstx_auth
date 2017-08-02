(ns us.edwardstx.auth.orchestrator
  (:require [com.stuartsierra.component :as component]
            [manifold.deferred :as d]

            [us.edwardstx.auth.authentication :as authentication]
            [us.edwardstx.auth.keys :as keys]))


(defrecord Orchestrator [a k]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-orchestrator []
  (map->Orchestrator {}))
