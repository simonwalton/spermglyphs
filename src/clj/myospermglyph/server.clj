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
           [:title "Ovii | Make your own Sperm Glyph"]
           [:link {:href "/assets/bootstrap/css/bootstrap.min.css" :rel "stylesheet"}]
           [:link {:href "/assets/bootstrap/css/bootstrap-theme.min.css" :rel "stylesheet"}]]
      [:body content]))
 
(defn view-content []
  (view-layout
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "/js/cljs.js"}]
    [:div {:class "container"}
      [:div {:class "jumbotron"}
        [:H1 "Make Your Own Sperm Glyph"]
        [:p "Use the parameters to change, or try a preset!"]
        [:div {:class "row"}
         ; left-hand col
          [:div {:class "col-md-6"}
            [:div {:id "spermdiv"}]]
         ; right-hand col
          [:div {:class "col-md-6"}
            [:ul {:class "nav nav-tabs"}
              [:li {:class "active"} [:a {:href "#manual" :data-toggle "tab"} "Manual"]]
              [:li [:a {:href "#preset" :data-toggle "tab"} "Presets"]]]
            [:div {:class "tab-content"}
              [:div {:class "tab-pane active" :id "manual"} "Manual"]
              [:div {:class "tab-pane" :id "preset"} "Presets"]]]]
     ]
    ]
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
