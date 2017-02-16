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

;;              [accountant.core :as accountant]
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
  (let [page (re-frame/subscribe [:page])]
    (fn []
      [:div
       [:h2 @page]
       [v/login-page #(re-frame/dispatch [:login %])]


       ])))

;; -------------------------
;; Routes

;;(secretary/defroute "/" []
;;  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/whoami" []
  (session/put! :current-page #'whoami-page))

(secretary/defroute "/" []
  (session/put! :current-page #'login-page))

;; -------------------------
;; Initialize app

(defn ^:export init! []
  (re-frame/dispatch-sync [:initialize])
  (re-frisk/enable-re-frisk!)
  (reagent/render [main-panel] (.getElementById js/document "app"))
)
;;  (accountant/configure-navigation!
;;    {:nav-handler
;;     (fn [path]
;;       (secretary/dispatch! path))
;;     :path-exists?
;;     (fn [path]
;;       (secretary/locate-route path))})
;;  (accountant/dispatch-current!)
;;  (mount-root))
