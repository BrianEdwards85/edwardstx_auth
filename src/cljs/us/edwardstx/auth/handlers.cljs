(ns us.edwardstx.auth.handlers
  (:require [re-frame.core :as re-frame]
            [us.edwardstx.auth.service :as service]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            ))
(enable-console-print!)

(def init-db
  {:page "______"
   :user :none})

(re-frame/reg-event-fx
 :initialize
 (fn [_ _]
   {:db init-db
    :http-xhrio {:method :get
                 :uri "/echo"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:update-page]}}))

(re-frame/reg-event-db
 :update-page
 (fn [db [_ r]]
     (assoc db :page (:uri r))))

(re-frame/reg-event-fx
 :login
 (fn [db [_ r]]
  ;; {:db db}
   (service/login r :login-succcess :login-failure)
   ))


(re-frame/reg-event-db
 :login-succcess
 (fn [db [_ r]]
   (js/console.log (str r))
   db
))
(re-frame/reg-event-db
 :login-failure
 (fn [db [_ r]]
   (js/console.log (str r))
   db))
