(ns us.edwardstx.auth.html
  (:require [hiccup.page :refer [include-js include-css html5]]
            [config.core :refer [env]]))


(def mount-target [:div#app [:div.loader ]])

(defn head [t]
  [:head
   (if t [:title t])
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:link
    {:type "text/css"
     :rel "stylesheet"
     :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.css"
;;     :integrity "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
     :crossorigin "anonymous"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head "loading")
    [:body {:class "body-container"}
     mount-target
     [:script
      {:src "https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"
  ;;     :integrity "sha256-Sk3sfKjyVntDJ8grhzyNfdd090uQCdL/ZUMagVRpPeo="
       :crossorigin "anonymous"
       :type "text/javascript"}]
     [:script
      {:src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
       :integrity "sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
       :crossorigin "anonymous"
       :type "text/javascript"}]
     (include-js "/js/app.js")]))



