(ns us.edwardstx.auth.core
    (:require [reagent.core :as reagent :refer [atom]]
              [us.edwardstx.auth.components :refer [login-page whoami-page]]
              [us.edwardstx.auth.handlers :as handlers]
              [us.edwardstx.auth.subs :as subs]
              [us.edwardstx.auth.views :as v]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [re-frame.core :as re-frame]
              [re-frisk.core :as re-frisk]

              [accountant.core :as accountant]
              ))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to auth!"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About auth!"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

(defn main-panel []
  (let [page (re-frame/subscribe [:page])
        user (re-frame/subscribe [:user])]
    (fn []
      (cond
        (or (= @page :loading) (= @user :none)) [:div {:class "loader"}]
        (= @page :whoami) [v/whoami user]
        :else [v/login-page #(re-frame/dispatch [:login %])]))))

;; -------------------------
;; Routes

(secretary/defroute "/about" []
  (re-frame/dispatch [:navigate :about]))

(secretary/defroute "/whoami" []
 (re-frame/dispatch [:navigate :whoami]))

(secretary/defroute "/" []
  (re-frame/dispatch [:navigate :root]))

;; -------------------------
;; Initialize app

(def accountant-configuration
  {:nav-handler
   (fn [path] (secretary/dispatch! path))
   :path-exists?
   (fn [path] (secretary/locate-route path))})

(defn ^:export init! []
  (re-frisk/enable-re-frisk!)
  (accountant/configure-navigation! accountant-configuration)
  (re-frame/dispatch-sync [:initialize])
  (accountant/dispatch-current!)
  (reagent/render [main-panel] (.getElementById js/document "app")))
