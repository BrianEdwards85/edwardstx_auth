(ns us.edwardstx.auth.views
  (:require [reagent.core  :as r] ;; :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

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

(defn login-page [login]
  (let [user (r/atom nil)
        pass (r/atom nil)
        auth (r/atom nil)]
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
            [bootstrap-input-group auth "inputAuth" "Auth" "number"]]
           [:button
            {:class "btn btn-lg btn-primary btn-block"
             :on-click #(login {:user @user :pass @pass :auth @auth})} "Login"]
           ]]]]])))
