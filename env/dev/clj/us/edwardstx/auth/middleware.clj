(ns us.edwardstx.auth.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (dissoc site-defaults :security))
      wrap-exceptions
      wrap-cookies
      wrap-reload))
