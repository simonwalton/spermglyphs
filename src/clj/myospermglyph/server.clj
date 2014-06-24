(ns myospermglyph.server
  (:require
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [clojure.math.numeric-tower :as math])
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
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
      [:p {:id "clickhere"} "Get yourself a nice alert by clicking here."]
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
              ; manual
              [:div {:class "tab-pane active" :id "manual"} 
                [:div {:class "form-horizontal"}
                  [:div {:class "form-group"}
                    [:label {:for "bcf" :class "col-sm-2 control-label"} "BCF"]
                    [:div {:class "col-sm-10"}
                      [:input {:type "range" :min "0" :max "50" :id "bcf"}]
                    ]
                  ]
                ]
              ]
              [:div {:class "tab-pane" :id "preset"} "Presets"]
            ]
          ]
        ]
      ]
    ]
    [:script {:src "/js/cljs.js"}]
    [:script "myospermglyph.server._init(); myospermglyph.server._draw();"]
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



