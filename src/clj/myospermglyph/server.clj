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
           [:link {:href "/assets/css/jquery.gridster.min.css" :rel "stylesheet"}]
           [:link {:href "//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" :rel "stylesheet"}]]
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
          [:a {:href (str "javascript:selectZooPreset('" img "');")} [:img {:src (str "assets/img/species/" img) :alt title}]]
          [:div {:class "caption"} title]
;              [:button {:type "button" :class "btn btn-primary btn-sm btn-zoo"}
 ;               [:span {:class "glyphicon glyphicon-eye-open"}] (str " " title)
  ;            ]
            ]])) 

(defn create-human-preset[id, img, title, desc, note]
  (view-layout
    [:div {:class "media sperm-preset-media-box" :id id}
      [:a {:class "pull-left human-preset-link" :href "#"}[:img {:class "media-object" :src (str "assets/img/human/" img) :alt title}]]
      [:div {:class "media-body"}
        [:a {:class "human-preset-link" :href (str "javascript:selectHumanPreset('" id "');")} [:h4 {:class "media-heading"} title]] [:p desc [:p {:class "sperm-note"}] [:i {:class "fa fa-info-circle"}] (str " " note)]
       ]]))

(defn create-navbar []
  (view-layout
    [:div {:class "navbar navbar-inverse navbar-fixed-top" :role "navigation"}
      [:div {:class "container"}
        [:div {:class "navbar-header"}
          [:button {:type "button" :class "navbar-toggle" :data-toggle "collapse" :data-target ".navbar-collapse"}
            [:span {:class "sr-only"} "Toggle navigation"]
          ]
          [:a {:class "navbar-brand" :href "#"}"Ovii"]
        ]
        [:div {:class "collapse navbar-collapse"}
          [:ul {:class "nav navbar-nav"}
            [:li {:class "active"}[:a {:href "#"} [:i {:class "fa fa-home"}]  " Home"]]
            [:li [:a {:href "#about"} [:i {:class "fa fa-question-circle"}] " About"]]
            [:li [:a {:href "#contact"} [:i {:class "fa fa-file-pdf-o fa-inverse"}] " Paper"]]
          ]
          [:ul {:class "nav navbar-right"}
            [:a {:href "http://github.com" :alt "View our code on Github!"}[:i {:class "fa fa-github-square"}]]
          ]
         ]]]))

(defn view-content []
  (view-layout
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "/assets/js/bootstrap-slider.js"}]
    [:script {:src "/assets/js/underscore-min.js"}]
    [:script {:src "/assets/js/jquery.gridster.min.js"}]
    
    (create-navbar)

    [:div {:class "container-main container"}
      [:div {:class "container"}
        [:div [:img {:src "assets/img/logo_logo.png" :class "logo" }]]
        [:div {:class "row"}
         ; left-hand col
          [:div {:class "col-md-6"}
            [:div {:class "gridster spermgrid"}
              [:ul
                [:li {:data-row 1 :data-col 1 :data-sizex 2 :data-sizey 2}[:div {:class "spermdiv spermdiv-selected" :id "spermbig"}]]
                [:li {:data-row 1 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall0"}]]
                [:li {:data-row 2 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall1"}]]
                [:li {:data-row 3 :data-col 1 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall2"}]]
                [:li {:data-row 3 :data-col 2 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall3"}]]
                [:li {:data-row 3 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall4"}]]
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
                [:div {:class "media"}
                  (create-human-preset "grade-a" "logo_grade-a.png" "Grade A" "Sperm with progressive motility. These cells are the strongest and swim fast in a straight line." "Note the relative agreement of the VCL, VAP and VSL measures.")
                  (create-human-preset "grade-b" "logo_grade-b.png" "Grade B" "These also move forward but tend to travel in a curved or crooked motion." "Note the VCL increasing noticably compared to the VAP as the cell meanders.")
                  (create-human-preset "grade-c" "logo_grade-c.png" "Grade C" "These have non-progressive motility because they do not move forward despite the fact that they move their tails." "Note that no VCL, VAP or VSL values are available in the data.")
                  (create-human-preset "grade-d" "logo_grade-d.png" "Grade D" "These are immotile and fail to move at all." "Note that no VCL, VAP or VSL values are available in the data.")
                ]
               ]
            ]]]]]
    [:script " var sliders = {}; var gridster; var papers = []; var selectedDiv = null; var selectedPaper = null;"]
    [:script {:src "/js/cljs.js"}]
    [:script "
        function getSlider(id) { return sliders[id]; }

        function setSelected(div, paper) {
          selectedPaper = paper;
          selectedDiv = div;
          console.log(div);
          $('.spermdiv').attr('class','spermdiv');
          div.addClass('spermdiv-selected');
        };

        function selectZooPreset(str) {
          console.log(str);
        };

        function selectHumanPreset(str) {
          myospermglyph.server._drawHumanPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
        };

        $(document).ready(function(){
          myospermglyph.server._init('/assets/data/'); 

          var cellsize = [170,170];
          var margins = [5,5];
          gridster = $('.gridster ul').gridster({
            widget_base_dimensions: cellsize,
            widget_margins: margins, 
            resize: {
              enabled: true, 
              resize: function(e, ui, $widget) {
                var me = $widget.children().first();
                myospermglyph.server._draw(me, [this.resize_coords.data.width, this.resize_coords.data.height]);
              },
            }
          }).data('gridster');

          $('.create-slider').each(function(i) {
            var sl = $(this).slider().on('slide', function(ev) {
              myospermglyph.server._update();
            }).data('slider');
            sliders[$(this).prop('id')] = sl;
          });

          papers = [];
          papers = _.union(papers, [myospermglyph.server._draw($('#spermbig'), [(cellsize[0]+margins[0])*2,(cellsize[1]+margins[1])*2])]);
          setSelected($('#spermbig'),papers[0]);
          
          for(i in [0,1,2,3,4])
            papers = _.union(papers, [myospermglyph.server._draw($('#spermsmall'+i.toString()), cellsize)]);

          _.each(papers, function(p) {
             $(p.canvas).css({'pointer-events': 'none'});
             $(p.canvas).parent().click(function(div) {  setSelected($(div.currentTarget),p); });
          });

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



