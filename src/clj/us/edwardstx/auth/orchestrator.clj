(ns us.edwardstx.auth.orchestrator
  (:require [com.stuartsierra.component :as component]
            [manifold.deferred :as d]

            [us.edwardstx.auth.authentication :as authentication]
            [us.edwardstx.auth.keys :as keys]))


(defrecord Orchestrator [db keys]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-orchestrator []
  (map->Orchestrator {}))

(defn issue-token [keys user]
  (let [claims (keys/extend-claims
                keys
                (keys/creat-claims user))]
    (assoc claims :token (keys/sign keys claims))))

(defn authenticate
  ([orchestrator {:keys [user pass auth]}]
   (authenticate orchestrator user pass auth))
  ([orchestrator user pass auth]
   (d/chain
    (authentication/authenticate (:db orchestrator) user pass auth)
    #(if % user (throw (Exception. "Authentication Failed")))
    #(issue-token (:keys orchestrator) %)
    )))

(defn service-token [orchestrator service ksr]
  (d/chain
   (authentication/validate-token (:db orchestrator) service ksr)
   #(keys/extend-claims (:keys orchestrator) %)
   #(keys/sign (:keys orchestrator) %)))
