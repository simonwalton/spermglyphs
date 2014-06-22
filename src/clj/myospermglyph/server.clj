(ns myospermglyph.server
  (:require
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response])
  (:use [hiccup.core]
        [compojure.core]))

(defn view-layout [& content]
  (html
      [:head
           [:meta {:http-equiv "Content-type"
                        :content "text/html; charset=utf-8"}]
           [:title "Project-Name"]]
      [:body content]))
 
(defn view-content []
  (view-layout
       [:h2 "Project-Name"]
       [:p {:id "clickhere"} "Get yourself a nice alert by clicking here."]
       [:script {:src "/assets/js/jquery.min.js"}]
       [:script {:src "/assets/js/raphael-min.js"}]
       [:script {:src "/js/cljs.js"}]
       [:script "myospermglyph.server.draw()"]
    ))

(defroutes main-routes
  (GET "/" []
      (view-content))
      (route/resources "/"))
 
(def app (handler/site main-routes))

(defroutes main-routes
  (GET "/" []
      (response/redirect "web-page-name.html"))
      (route/resources "/"))
