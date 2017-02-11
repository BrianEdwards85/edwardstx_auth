(ns auth.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.cookies :refer [wrap-cookies]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (dissoc site-defaults :security))
      wrap-cookies))
