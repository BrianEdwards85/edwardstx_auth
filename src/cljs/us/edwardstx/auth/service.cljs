(ns us.edwardstx.auth.service
  (:require [ajax.core :as ajax]))

(def json (ajax/json-response-format {:keywords? true}))



(defn login [auth succcess failure]
  (let [body (.stringify js/JSON (clj->js auth))]
    {:http-xhrio {:method          :post
                  :uri             "/auth/api/auth"
                  :body            body
                  :format          (ajax/json-request-format)
                  :response-format json
                  :headers         {:content-type "application/json"}
                  :on-success      [succcess]
                  :on-failure      [failure]}}))

(defn validate [valid-user no-user]
  {:http-xhrio {:method          :get
                :uri             "/auth/api/validate"
                :response-format json
                :on-success      [valid-user]
                :on-failure      [no-user]}})


;;(defn send-auth [auth]
;;  (http/post "/auth" {:json-params auth}))

;;(defn get-current-user [succcess failure]
;;  {:http-xhrio {:method :get
;;                :uri "/validate"
;;                :response-format json
;;                :on-success [succcess]
;;                :on-failure [failure]}})


;;(defn send-auth [auth]
;;  (let [json (clj->js auth)
;;        s (.stringify js/JSON json)]
;;    (POST "/auth" {:body s :headers {:content-type "application/json"}})))

;;(defn get-auth []
;;  (http/get "/validate"))
