(ns us.edwardstx.auth.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :user
 (fn [db _]
   (:user db)))

(re-frame/reg-sub
 :page
 (fn [db _]
   (:page db)))

