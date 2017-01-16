(ns us.edwardstx.auth.prod
  (:require [us.edwardstx.auth.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
