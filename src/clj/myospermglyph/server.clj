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

(defn create-thumbnail[img, title]
  (view-layout
      [:div {:class "col-xs-6 col-md-3 zoo-thumbnail-container"}
        [:div {:class "thumbnail"}
          [:img {:src img :alt title}
            [:div {:class "caption"}
              [:button {:type "button" :class "btn btn-primary btn-sm btn-zoo"}
                [:span {:class "glyphicon glyphicon-eye-open"}] (str " " title)
              ]
            ]]]])) 

(defn view-content []
  (view-layout
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "/assets/js/bootstrap-slider.js"}]
    [:script {:src "/assets/js/underscore-min.js"}]
    [:div {:class "container"}
      [:div {:class "container"}
        [:div {:class "row"}
          [:div {:class "pull-left"} [:img {:src "assets/img/logo.png" :class "logo" :width "120" :height "120"}]]
          [:div {:class "pull-left"} 
            [:H1 {:class "titlea" } "Make Your Own"]
            [:H1 {:class "titleb" } "Sperm Glyph"]
          ]
        ]
        [:div {:class "row"}
         ; left-hand col
          [:div {:class "col-md-6"}
            [:div {:id "spermdiv"}]]
         ; right-hand col
          [:div {:class "col-md-6 right-controls"}
            [:ul {:class "nav nav-tabs"}
              [:li {:class "active"} [:a {:href "#manual" :data-toggle "tab"} "Manual"]]
              [:li  [:a {:href "#preset" :data-toggle "tab"} "Sperm Zoo"]]]
            [:div {:class "tab-content"}
              ; manual
              [:div {:class "tab-pane active" :id "manual"} 
                [:div {:class "row"}
                  ; kinematics
                  [:div {:class "col-sm-6"}
                    [:div {:class "bs-callout bs-callout-kinematics"}
                      [:h4 "Kinematics"]
                      [:p "Movement characteristics of the head"]
                        [:div {:class "form-horizontal"}
                        (spermcontrol-slider "vcl" "80" "300")
                        (spermcontrol-slider "vap" "30" "70")
                        (spermcontrol-slider "vsl" "10" "30")
                        (spermcontrol-slider "bcf" "0" "50")
                      ]
                    ]
                  ]
                  ; Mechanics
                  [:div {:class "col-sm-6"}
                    [:div {:class "bs-callout bs-callout-mechanics"}
                      [:h4 "Mechanics"]
                      [:p "Mechanics of the flagella"]
                        [:div {:class "form-horizontal"}
                        (spermcontrol-slider "fta" "80" "300")
                        (spermcontrol-slider "ftc" "30" "70")
                        (spermcontrol-slider "ftt" "10" "30")
                        (spermcontrol-slider "fas" "0" "50")
                      ]
                    ]
                  ]
                ]
                [:div {:class "row"}
                  ; Morphological
                  [:div {:class "col-sm-6"}
                    [:div {:class "bs-callout bs-callout-morphological"}
                      [:h4 "Morpholigcal"]
                      [:p "Head characteristics"]
                        [:div {:class "form-horizontal"}
                        (spermcontrol-slider "hl" "80" "300")
                        (spermcontrol-slider "hw" "30" "70")
                        (spermcontrol-slider "hr" "10" "30")
                      ]
                    ]
                  ]
                  ; uncertainty
                  [:div {:class "col-sm-6"}
                    [:div {:class "bs-callout bs-callout-uncertainty"}
                      [:h4 "Uncertainty"]
                      [:p "Machine vision capture uncertainty"]
                        [:div {:class "form-horizontal"}
                        (spermcontrol-slider "uc" "80" "300")
                        (spermcontrol-slider "uf" "30" "70")
                      ]
                    ]
                  ]
                ]
              ]
             ; zoo
              [:div {:class "tab-pane" :id "preset"}
                [:div {:class "row"}
                  (create-thumbnail "assets/img/species/rat.jpg" "Rat")
                  (create-thumbnail "assets/img/species/mouse.jpg" "Mouse")
                  (create-thumbnail "assets/img/species/rabbit.jpg" "Rabbit")
                  (create-thumbnail "assets/img/species/hamster.jpg" "Marmoset")
                ]
               [:div {:class "row"}
                  (create-thumbnail "assets/img/species/boar.jpg" "Boar")
                  (create-thumbnail "assets/img/species/bull.jpg" "Bull")
                  (create-thumbnail "assets/img/species/marmoset.jpg" "Marmoset")
                  (create-thumbnail "assets/img/species/donkey.jpg" "Donkey")
                ]
              [:div {:class "row"}
                  (create-thumbnail "assets/img/species/spermwhale.jpg" "Sperm Whale")
                  (create-thumbnail "assets/img/species/cat.jpg" "Cat")
                  (create-thumbnail "assets/img/species/gazelle.jpg" "Gazelle")
                ]
              ]
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



