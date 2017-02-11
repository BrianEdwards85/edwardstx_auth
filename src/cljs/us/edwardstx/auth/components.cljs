(ns us.edwardstx.auth.components
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r :refer [atom]]
            [cljs.core.async :refer [<!]]
            [us.edwardstx.auth.service :as service]
            [us.edwardstx.auth.quark :refer [quark]]))

(defn rest-map [a b]
  (apply hash-map a b))

(defn input-element
  ([a p]
   [:input (merge p {:value @a
                     :on-change #(reset! a (-> % .-target .-value))})])
  ([a p & n]
   (input-element a (rest-map p n))))

(defn bootstrap-input-element
  ([a p]
   (input-element a (merge p {:class "form-control"})))
  ([a p & n]
   (bootstrap-input-element a (rest-map p n))))

(defn bootstrap-input-group [a name label type]
  [:div.form-group
   [:h4 {:for name} label]
   (bootstrap-input-element a :placeholder label :type type :id name :name)])


(defn login [m]
  (do
    (service/send-auth m)))

(defn login-page []
  (let [cred (atom {:user nil :pass nil :auth nil})
        user (quark cred [:user] atom)
        pass (quark cred [:pass] atom)
        auth (quark cred [:auth] atom)] 
    (fn []
      [:div.container {:id  "login-container"}
       [:div.row
        [:div {:class "col-md-6"}
         [:div {:class "panel panel-login"}
          [:div.panel-heading
           [:h2 "Sign in please"]]
          [:div.panel-body
           [:form.panel  ;;.form-horizontal
            [bootstrap-input-group user "inputEmail" "Email" "email"]
            [bootstrap-input-group pass "inputPassword" "Password" "password"]
            [bootstrap-input-group auth "inputAuth" "Auth" "number"]
           ]
           [:button {:class "btn btn-lg btn-primary btn-block" :on-click #(login @cred)} "Login"]
           ]]
         ]]])))

(defn whoami-panel [details]
  [:div.row
   [:div {:class "col-md-6"}
    [:div {:class "panel panel-login"}
     [:div.panel-heading
      [:h2.text-center "Welcome"]]
     (if (<= 3 (count details))
       [:div.panel-body
        [:div.row
         [:div.col-md-2 [:h3 "User"]]
         [:div.col-md-4 (:sub details)]]
        [:div.row
         [:div.col-md-2 [:h3 "Session"]]
         [:div.col-md-4 (:jti details)]]
        [:div.row
         [:div.col-md-2 [:h3 "Issuer"]]
         [:div.col-md-4 (:iss details)]]])]]])




(defn whoami-page []
  (let [details (atom {})]
    (r/create-class
     {:component-did-mount
      #(go (reset! details (:body (<! (service/get-auth)))))

      :reagent-render
      (fn []
        [:div.container {:id "whoami-container"}
         [whoami-panel @details]])
      })))
