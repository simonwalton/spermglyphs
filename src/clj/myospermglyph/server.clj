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
           [:link {:href "/assets/css/slider.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/jquery.gridster.min.css" :rel "stylesheet"}]]
      [:body content]))


(defn create-slider[property minval maxval]
  (view-layout 
      [:div {:class "form-group"}
        [:label {:for property :class "col-sm-2 control-label"} property]
        [:div {:class "col-sm-10"}
          [:input {:type "text" :data-slider-min (str "\"" minval "\"") :data-slider-max (str "\"" maxval "\"") :id property :class "create-slider"}]
        ]]
      ))

(defn create-slider-group[id nicename desc sliders]
  (view-layout
    [:div {:class "col-sm-6"}
      [:div {:class (str "bs-callout bs-callout-" id)}
        [:h4 nicename]
        [:p desc]
          [:div {:class "form-horizontal"}
            (map (fn [x] (create-slider (:id x) (:min x) (:max x))) sliders)
        ]]]))

(defn create-thumb[img, title]
  (view-layout
      [:div {:class "col-xs-6 col-md-3 zoo-thumbnail-container"}
        [:div {:class "thumbnail"}
          [:img {:src (str "assets/img/species/" img) :alt title}]
          [:div {:class "caption"} title]
;              [:button {:type "button" :class "btn btn-primary btn-sm btn-zoo"}
 ;               [:span {:class "glyphicon glyphicon-eye-open"}] (str " " title)
  ;            ]
            ]])) 

(defn view-content []
  (view-layout
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "/assets/js/bootstrap-slider.js"}]
    [:script {:src "/assets/js/underscore-min.js"}]
    [:script {:src "/assets/js/jquery.gridster.min.js"}]
    [:div {:class "container"}
      [:div {:class "container"}
        [:div {:class "row"}
          [:div {:class "pull-left"} [:img {:src "assets/img/logo.png" :class "logo" :width "120" :height "120"}]]
          [:div {:class "pull-left"} 
            [:H1 {:class "titlea" } "Make Your Own"]
            [:H1 {:class "titleb" } "Sperm Glyph"]
          ]]
        [:div {:class "row"}
         ; left-hand col
          [:div {:class "col-md-6"}
            [:div {:class "gridster spermgrid"}
              [:ul
                [:li {:data-row 1 :data-col 1 :data-sizex 2 :data-sizey 2} [:div {:id "spermbig"}]]
                [:li {:data-row 1 :data-col 3 :data-sizex 1 :data-sizey 1} [:div {:id "spermsmall0"}]]
                [:li {:data-row 2 :data-col 3 :data-sizex 1 :data-sizey 1} [:div {:id "spermsmall1"}]]
                [:li {:data-row 3 :data-col 1 :data-sizex 1 :data-sizey 1} [:div {:id "spermsmall2"}]]
                [:li {:data-row 3 :data-col 2 :data-sizex 1 :data-sizey 1} [:div {:id "spermsmall3"}]]
                [:li {:data-row 3 :data-col 3 :data-sizex 1 :data-sizey 1} [:div {:id "spermsmall4"}]]
              ]
              
            ]
           ]
         ; right-hand col
          [:div {:class "col-md-6 right-controls"}
            [:ul {:class "nav nav-tabs"}
              [:li {:class "active"} [:a {:href "#manual" :data-toggle "tab"} "Manual"]]
              [:li [:a {:href "#zoo" :data-toggle "tab"} "Sperm Zoo"]]
              [:li [:a {:href "#human" :data-toggle "tab"} "Human Presets"]]]
            [:div {:class "tab-content"}
            ; manual
              [:div {:class "tab-pane active" :id "manual"} 
                [:div {:class "row"}
                  (create-slider-group "kinematics" "Kinematics" "Movement characteristics of the head"
                    [{:id "vcl" :min 80 :max 300} {:id "vcl" :min 80 :max 300}])
                  (create-slider-group "mechanics" "Mechanics" "Mechanics of the flagella"
                    [{:id "fta" :min 80 :max 300} {:id "ftc" :min 80 :max 300} {:id "ftt" :min 80 :max 300} {:id "fas" :min 80 :max 300}])
                ]
                [:div {:class "row"}
                  (create-slider-group "morphological" "Kinematics" "Head characteristics"
                    [{:id "hl" :min 80 :max 300} {:id "hw" :min 30 :max 70} {:id "hr" :min 10 :max 30}])
                  (create-slider-group "uncertainty" "Uncertainty" "Machine vision uncertainty"
                    [{:id "uc" :min 80 :max 300} {:id "uf" :min 30 :max 70}])
                ]
              ]
            ; zoo
              [:div {:class "tab-pane" :id "zoo"}
                [:div {:class "row"}
                  (create-thumb "rat.jpg" "Rat") (create-thumb "mouse.jpg" "Mouse") (create-thumb "rabbit.jpg" "Rabbit") (create-thumb "hamster.jpg" "Marmoset")]
                [:div {:class "row"}
                  (create-thumb "boar.jpg" "Boar") (create-thumb "bull.jpg" "Bull") (create-thumb "marmoset.jpg" "Marmoset") (create-thumb "donkey.jpg" "Donkey")]
                [:div {:class "row"}
                  (create-thumb "spermwhale.jpg" "Sperm Whale") (create-thumb "cat.jpg" "Cat") (create-thumb "gazelle.jpg" "Gazelle")]
              ]
            ; human presets
              [:div {:class "tab-pane" :id "human"}
                [:div {:class "row"}
                  (create-thumb "rat.jpg" "Rat") (create-thumb "mouse.jpg" "Mouse") (create-thumb "rabbit.jpg" "Rabbit") (create-thumb "hamster.jpg" "Marmoset")]
                [:div {:class "row"}
                  (create-thumb "boar.jpg" "Boar") (create-thumb "bull.jpg" "Bull") (create-thumb "marmoset.jpg" "Marmoset") (create-thumb "donkey.jpg" "Donkey")]
                [:div {:class "row"}
                  (create-thumb "spermwhale.jpg" "Sperm Whale") (create-thumb "cat.jpg" "Cat") (create-thumb "gazelle.jpg" "Gazelle")]
              ]
            ]]]]]
    [:script " var sliders = {}; var gridster; "]
    [:script {:src "/js/cljs.js"}]
    [:script "
        function getSlider(id) { return sliders[id]; }

        $(document).ready(function(){
          var cellsize = [170,170];
          gridster = $('.gridster ul').gridster({
            widget_base_dimensions: cellsize,
            widget_margins: [5, 5],
            helper: 'clone',
            resize: {
              enabled: true
            }
          }).data('gridster');

          $('.js-resize-random').on('click', function() {
              gridster.resize_widget(gridster.$widgets.eq(getRandomInt(0, 9)),
                  getRandomInt(1, 4), getRandomInt(1, 4))
          });
          
          $('.create-slider').each(function(i) {
            var sl = $(this).slider().on('slide', function(ev) {
              myospermglyph.server._update();
            }).data('slider');
            sliders[$(this).prop('id')] = sl;
          });

          myospermglyph.server._draw($('#spermbig'), [cellsize[0]*2,cellsize[1]*2]);
          for(i in [0,1,2,3,4])
            myospermglyph.server._draw($('#spermsmall'+i.toString()), cellsize);
      });
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



