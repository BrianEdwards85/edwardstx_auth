(ns us.edwardstx.auth.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [us.edwardstx.auth.middleware :refer [wrap-middleware]]
            [clojure.data.json :as json]
            [manifold.deferred :as d]
            [config.core :refer [env]]
            [us.edwardstx.common.uuid :refer [uuid]]
            [us.edwardstx.common.jwt :refer [wrap-jwt]]
            [us.edwardstx.auth.core :refer [conf]]
            [us.edwardstx.auth.authentication :as authentication]
            [us.edwardstx.auth.token :as token]
            ))

(def semaphore (d/deferred))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])


(defn head [t]
  [:head
   (if t [:title t])
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
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

(def authentication-failed
  {:status 401 :body "Authentication Failed" :headers {"Content-Type" "text/plain"}})

(def invalid-token
  {:status 400 :body "Invalid Token" :headers {"Content-Type" "text/plain"}})

(defn read-body [r]
  (->> r
       :body
       slurp))

(defn auth [r]
  (let [s (read-body r) 
        j (json/read-str s :key-fn keyword)
        cred (if (string? (:auth j)) (assoc j :auth (read-string (:auth j))) j)]
    (if (authentication/authenticate cred)
      (let [sid (uuid)
            t (token/issue-token (:user cred) sid)]
        {:status 200
         :body (json/write-str {:user (:user cred) :sid sid :token t})
         :headers {"Content-Type" "application/json"}
         :cookies {"uid" {:value t :domain ".edwardstx.us" :max-age 86000}}})
      authentication-failed)))

(defn pub-key []
  (let [key (-> conf :jwt :public-key)
        header (-> conf :jwt :headder)]
    (json/write-str {:key key :header header})))

(defn validate-post [r]
  (let [t (read-body r)]
    (if-let [v (token/unsign t)]
      (json/write-str v)
      invalid-token)))

(defn validate-get [r]
  (if-let [v (:jwt r)]
    {:status 200
     :body (json/write-str v)
     :headers {"Content-Type" "application/json"}}
    invalid-token))

(defn echo [r]
  (do
    {:body (json/write-str (dissoc  r :body))
     :headers {"Content-Type" "application/json" "Extra" "No"}
     :status 200}))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/whoami" [] (loading-page))
  (POST "/auth" [] auth )
  (POST "/validate" [] validate-post)
  (GET "/validate" [] validate-get)
  (GET "/about" [] (loading-page))
  (GET "/key" [] (pub-key))
  (GET "/echo" [] echo)
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware (wrap-jwt #'routes)))
