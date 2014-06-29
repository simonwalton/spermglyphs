(ns myospermglyph.server
  (:require
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [clojure.math.numeric-tower :as math]
            [clojure.data.json :as json]
            [clojure.string :as string]
    )
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
           [:link {:href "/assets/css/slider.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/main.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/jquery.gridster.min.css" :rel "stylesheet"}]
           [:link {:href "//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" :rel "stylesheet"}]]
      [:body content]))


(defn create-slider[s]
  (let [step (get s :step 1.0)]
  (view-layout 
      [:div {:class "form-group"}
        [:label {:for (:id s) :class "control-label"} (:name s)] [:span {:class "control-desc"} (str " " (:desc s))]
        [:div {:style "width:100%"} [:input {:type "text" :data-slider-min (str (:min s)) :data-slider-step (str step) :data-slider-max (str (:max s)) :id (:id s) :class "create-slider"}]]]
      )))

(defn create-slider-group[id nicename desc sliders]
  (view-layout
      [:div {:class (str "bs-callout bs-callout-" id)}
        [:h4 nicename]
        [:p desc]
          [:div {:class "form-horizontal"}
            (map (fn [x] (create-slider x)) sliders)
        ]]))

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

(defn create-zoo []
  (let [obj (json/read-str (slurp "resources/public/assets/data/animal.json") :key-fn keyword)
        rows (doall (map (fn [k] (create-thumb (:img (second k)) (:name (second k)))) obj))]
    (view-layout "<div class=\"row\">" 
      (map-indexed (fn[i x] (if (= 0 (mod i 4)) (str "</div><div class=\"row\">" x) x)) rows)
       "</div>"            
    )))

(defn create-human-preset[id, img, title, desc, note]
  (let [img (if (not (string/blank? img)) img "logo_default.png")]
   (view-layout
    [:div {:class "media sperm-preset-media-box" :id id}
      [:a {:class "pull-left human-preset-link" :href "#"}[:img {:class "media-object" :src (str "assets/img/human/" img) :alt title}]]
      [:div {:class "media-body"}
        [:a {:class "human-preset-link" :href (str "javascript:selectHumanPreset('" id "');")} [:h4 {:class "media-heading"} title]] [:p desc [:p {:class "sperm-note"}] 
          (if (not (string/blank? note)) [:span [:i {:class "fa fa-info-circle"}] (str " " note)] "")
       ]]])))
                                                  
(defn create-human-presets []
  (let [obj (json/read-str (slurp "resources/public/assets/data/human.json") :key-fn keyword)
        rows (doall (map (fn [k] (create-human-preset (first k) (:img (second k)) (:name (second k)) (:desc (second k)) (:note (second k)) )) obj))]
    (view-layout [:div {:class "media"} [:div {:class "media"} rows ]])))
                
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
                  [:div {:class "col-sm-6"}
                    (create-slider-group "kinematics" "Kinematics" "Movement of the head"
                      [{:id "vcl" :name "VCL" :desc "Curvilinear Velocity, &micro;<i>m</i>/s" :min 20 :max 400}
                       {:id "vsl" :name "VSL" :desc "Straight-line Velocity, &micro;<i>m</i>/s" :min 20 :max 400}
                       {:id "vap" :name "VAP" :desc "Average Path Velocity, &micro;<i>m</i>/s" :min 20 :max 400}
                       {:id "bcf" :name "BCF" :desc "Beat Cross Frequency <i>Hz</i>" :min 0 :max 50}
                       {:id "alh" :name "ALH" :desc "Amp. of Lateral Head Disp. &micro;<i>m</i>" :min 0 :max 50}
                       {:id "mad" :name "MAD" :desc "Mean Anglular Displacement, &deg;" :min 0 :max 60}])
                    (create-slider-group "uncertainty" "Uncertainty" "Machine vision uncertainty"
                      [{:id "headuncertainty" :name "Head" :desc "In capturing the head" :min 80 :max 300}
                      {:id "uf" :name "Flagella" :desc "In capturing the flagella" :min 30 :max 70}])
                  ]
                  [:div {:class "col-sm-6"}
                    (create-slider-group "mechanics" "Mechanics" "Mechanics of the flagella"
                      [{:id "fta" :name "FTA" :desc "Total Projected Arclength, &micro;<i>m</i>" :min 20 :max 400}
                       {:id "ftc" :name "FTC" :desc "Change in Angle, &deg;" :min 0 :max 100}
                       {:id "ftt" :name "FTT" :desc "Total Torque, <i>N</i>&micro;" :min 80 :max 300}
                       {:id "fas" :name "FAS" :desc "Asymmetry" :min -1 :max 1 :step 0.1}])
                     (create-slider-group "morphological" "Morphological" "Head characteristics"
                      [{:id "headlength" :name "Length" :desc "The length of the head" :min 80 :max 300} 
                       {:id "headwidth" :name "Width" :desc "The width of the head" :min 30 :max 70}
                       {:id "headrotation" :name "Rotation" :desc "The head's rotation, &deg" :min -50 :max 50}])
                     ]
                  ]
                ]
            ; zoo
              [:div {:class "tab-pane" :id "zoo"} (create-zoo) ]
            ; human presets
              [:div {:class "tab-pane" :id "human"} (create-human-presets) ]
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

        function manualProps() {
          var obj = {};
          for(key in sliders) {
            var sl = sliders[key];
            obj[key] = parseFloat(sl.getValue());
          }
          return obj;
        };

        $(document).ready(function() {
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
              myospermglyph.server._drawManual(selectedDiv, [selectedDiv.width(), selectedDiv.height()], manualProps());
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



