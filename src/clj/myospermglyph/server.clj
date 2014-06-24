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
           [:link {:href "/assets/bootstrap/css/bootstrap-theme.min.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/main.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/slider.css" :rel "stylesheet"}]]
      [:body content]))


(defn spermcontrol-slider[property, minval, maxval]
  (view-layout 
      [:div {:class "form-group"}
        [:label {:for property :class "col-sm-2 control-label"} property]
        [:div {:class "col-sm-10"}
          [:input {:type "text" :data-slider-min minval :data-slider-max maxval :id property :class "spermcontrol-slider"}]
        ]]
      ))


(defn view-content []
  (view-layout
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "/assets/js/bootstrap-slider.js"}]
    [:script {:src "/assets/js/underscore-min.js"}]
    [:div {:class "container"}
      [:div {:class "container"}
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
                  (spermcontrol-slider "vcl" "0" "50")
                  (spermcontrol-slider "vap" "0" "50")
                  (spermcontrol-slider "vsl" "0" "50")
                  (spermcontrol-slider "bcf" "0" "50")
                ]
              ]
              [:div {:class "tab-pane" :id "preset"} "Presets"]
            ]
          ]
        ]
      ]
    ]
    [:script " var sliders = {} "]
    [:script {:src "/js/cljs.js"}]
    [:script "
        function getSlider(id) { return sliders[id]; }
        $('.spermcontrol-slider').each(function(i) {
          var sl = $(this).slider().on('slide', function(ev) {
            myospermglyph.server._update();
          }).data('slider');
          sliders[$(this).prop('id')] = sl;
        });
        myospermglyph.server._init();
        myospermglyph.server._draw();
        "]
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



