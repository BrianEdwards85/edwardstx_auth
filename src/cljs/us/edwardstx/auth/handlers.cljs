(ns us.edwardstx.auth.handlers
  (:require [re-frame.core :as re-frame]
            [us.edwardstx.auth.service :as service]
            [day8.re-frame.http-fx]
            [accountant.core :as accountant]
            [clojure.string :as s]
            [cemerick.url :as url]))

(defn redirect [db]
  (let [r (or (:redirect db) "/whoami")]
    (if (s/starts-with? r "http")
      (.assign js/location r)
      (accountant/navigate! r))))

(defn init-db []
  {:page :loading
   :user :none
   :error nil
   :redirect (-> js/window
                 .-location
                 .-href
                 url/url
                 :query
                 (get "r"))})

(re-frame/reg-event-fx
 :initialize
 (fn [_ _]
   (merge
    (service/validate :valid-user :no-user)
    {:db (init-db)})))

(re-frame/reg-event-db
 :valid-user
 (fn [db [_ r]]
   (redirect db)
     (assoc db :user r)))

(re-frame/reg-event-db
 :no-user
 (fn [db [_ _]]
   (assoc db :user nil)))

(re-frame/reg-event-db
 :navigate
 (fn [db [_ r]]
   (assoc db :page r)))

(re-frame/reg-event-fx
 :login
 (fn [db [_ r]]
   (merge
    (service/login r :valid-user :login-failure)
    {:db (assoc db :error nil)})))

;;(re-frame/reg-event-db
;; :login-succcess
;; (fn [db [_ r]]
;;   (redirect db)
;;   db))

(re-frame/reg-event-db
 :login-failure
 (fn [db [_ r]]
   (assoc db :error "Login faild")))
