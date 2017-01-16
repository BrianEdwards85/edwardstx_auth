(ns us.edwardstx.auth.service
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn send-auth [auth]
  (http/post "/auth" {:json-params auth})
  )
;;(defn send-auth [auth]
;;  (let [json (clj->js auth)
;;        s (.stringify js/JSON json)]
;;    (POST "/auth" {:body s :headers {:content-type "application/json"}})))

(defn get-auth []
  (http/get "/validate"))
