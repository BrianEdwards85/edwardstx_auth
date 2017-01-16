(ns us.edwardstx.auth.core
  (:require [us.edwardstx.common.conf :as c]))

(def service-name "auth")

(def conf (c/get-conf service-name))
