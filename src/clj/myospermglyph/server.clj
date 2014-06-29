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
           [:link {:href "/assets/css/cover.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/main.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/d3.parcoords.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/jquery.gridster.min.css" :rel "stylesheet"}]
           [:link {:href "/assets/css/slick.grid.css" :rel "stylesheet"}]
           [:link {:href "//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" :rel "stylesheet"}]]
      [:body content]))


(defn create-slider[s]
  (let [step (get s :step 1.0)]
  (html
      [:div {:class "form-group"}
        [:label {:for (:id s) :class "control-label"} (:name s)] [:span {:class "control-desc"} (str " " (:desc s))]
        [:div {:style "width:100%"} [:input {:type "text" :data-slider-min (str (:min s)) :data-slider-step (str step) :data-slider-max (str (:max s)) :id (:id s) :class "create-slider"}]]]
      )))

(defn create-slider-group[id nicename desc sliders]
  (html
      [:div {:class (str "bs-callout bs-callout-" id)}
       [:img {:src "assets/img/logo_icon-footer.png"}][:div [:h4 nicename] [:div desc]]
          [:div {:class "form-horizontal"}
            (map (fn [x] (create-slider x)) sliders)
        ]]))

(defn create-thumb[id img title desc]
  (html
    [:div {:class "col-xs-6 col-md-3 zoo-thumbnail-container"}
        [:div {:class "thumbnail"}
          [:a {:title desc :id id :class "animal-preset-link":href (str "javascript:selectZooPreset('" img "');")} [:img {:src (str "assets/img/species/" img) :alt desc}]]
          [:div {:class "caption"} title]
;              [:button {:type "button" :class "btn btn-primary btn-sm btn-zoo"}
 ;               [:span {:class "glyphicon glyphicon-eye-open"}] (str " " title)
  ;            ]
            ]]))

(defn create-human-preset[ids, img, title, desc, note]
  (let [img (if (not (string/blank? img)) img "logo_default.png")]
   (html
    [:div {:class "media sperm-preset-media-box" }
      [:a {:class "pull-left human-preset-link" :href "#"}[:img {:class "media-object" :src (str "assets/img/human/" img) :alt title}]]
      [:div {:class "media-body"}
        [:a {:class "human-preset-link" :id ids :href "#"} [:h4 {:class "media-heading"} title]] [:p desc [:p {:class "sperm-note"}] 
          (if (not (string/blank? note)) [:span [:i {:class "fa fa-info-circle"}] (str " " note)] "")
       ]]])))
                                                  

(defn get-zoo-data []
  (into (sorted-map-by compare) (json/read-str (slurp "resources/public/assets/data/animal.json") :key-fn keyword)))

(defn get-human-data []
  (json/read-str (slurp "resources/public/assets/data/human.json") :key-fn keyword))

(defn create-zoo []
  (let [rows (get-zoo-data)
        rows (doall (map (fn [k] (create-thumb (first k) (:img (second k)) (:name (second k)) (:desc (second k)))) rows))]
    (html "<div class=\"row zoo-row\">" 
      (map-indexed (fn[i x] (if (= 0 (mod i 4)) (str "</div><div class=\"row zoo-row\">" x) x)) rows)
       "</div>"            
    )))

(defn create-human-presets []
  (let [rows (get-human-data)
        rows (doall (map (fn [k] (create-human-preset (first k) (:img (second k)) (:name (second k)) (:desc (second k)) (:note (second k)) )) rows))]
    (html [:div {:class "media"} [:div {:class "media"} rows ]])))

(defn all-data-json [] 
  (json/write-str (map (fn [k] (merge (second k) {:id (first k)} )) (merge (doall (get-human-data)) (doall (get-zoo-data))))))

(defn create-navbar []
  (html
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
    [:script {:src "/assets/js/d3.v2.js"}]
    [:script {:src "/assets/js/d3.parcoords.js"}]
    [:script {:src "/assets/js/pc-filter.js"}]
    
    (create-navbar)

    [:div {:class "container-outer container"}
      [:div {:class "container container-main"}
        [:div {:class "row"}
         ; left-hand col
          [:div {:class "col-md-6"}
            [:div [:img {:src "assets/img/logo_logo.png" :class "logo" }]]
            [:div {:id "left-grid" :class "gridster spermgrid"}
              [:ul
                [:li {:data-row 1 :data-col 1 :data-sizex 2 :data-sizey 2}[:div {:class "spermdiv spermdiv-selected" :id "spermbig"}]]
                [:li {:data-row 1 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall0"}]]
                [:li {:data-row 2 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall1"}]]
                [:li {:data-row 3 :data-col 1 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall2"}]]
                [:li {:data-row 3 :data-col 2 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall3"}]]
                [:li {:data-row 3 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall4"}]]
              ] 
            ]
            [:div {:id "left-explore" :class ""}

             ]
           ]
         ; right-hand col
          [:div {:class "col-md-6 right-controls"}
            [:ul {:class "nav nav-tabs"}
              [:li {:class "active"} [:a {:href "#manual" :data-toggle "tab"} "Manual"]]
              [:li [:a {:href "#zoo" :data-toggle "tab"} "Sperm Zoo"]]
              [:li [:a {:href "#human" :data-toggle "tab"} "Human Presets"]]
              [:li [:a {:href "#explore" :data-toggle "tab"} "Explore"]]]
            [:div {:class "tab-content"}
            ; manual
              [:div {:class "fade in tab-pane active" :id "manual"} 
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
              [:div {:class "fade tab-pane" :id "zoo"} [:h2 "Sperm Zoo"] [:p "Click an animal to see its representitive sperm glyph! Scroll down to see more animals." (create-zoo) ]]
            ; human presets
              [:div {:class "fade tab-pane" :id "human"}[:h2 "Human Presets"] [:p "Click an item to see its the sperm glyph for a human sperm in that category."  (create-human-presets) ]]
            ; filter
              [:div {:class "fade tab-pane" :id "explore"}[:h2 "Explore Dimensions"] [:p "Each axis in the plot is a dimension of the cell data. You can brush to filter the sperm matching those properties, which will appear to the left."]
                [:div {:id "explore-pc" :class "parcoords"}]
                [:div {:id "explore-grid"}]
             ]]]]]]
       ; bottom content
       [:div {:class "footer clearfix"} 
       [:div {:class "footer-inner"} 
        [:div {:class "row clearfix"}
          [:div {:class "inner cover col-md-4"}
           [:img {:src "assets/img/logo_icon-footer.png" :class "footer-img"}] [:h1 {:class "cover-heading"} "What is it?"]
            [:p {:class "lead"} "Our glyph design encodes a large collection of numerical measurements of a sperm cell to summarize its complex spatiotemporal motion characteristics."]
              [:a {:href "/about" :class "btn btn-md btn-default"}"Learn More"] 
           ]
          [:div {:class "inner cover col-md-4"}
           [:img {:src "assets/img/logo_icon-footer.png" :class "footer-img"}]
           [:h1 {:class "cover-heading"} "Our Paper"]
            [:p {:class "lead"} "For more information on our technique, including how the attributes are defined and encoded, please see our TVCG paper, to be presented at VIS 2014."]
              [:a {:href "/assets/paper/paper.pdf" :class "btn btn-md btn-default"}"Read the PDF"]
          ]
          [:div {:class "inner cover col-md-4"}
           [:img {:src "assets/img/logo_icon-footer.png" :class "footer-img"}]
           [:h1 {:class "cover-heading"} "Who are we?"]
            [:p {:class "lead"} "Good question. We are the visualization group at Oxford University's e-Research Centre (OeRC), and are led by Professor Min Chen."] 
              [:a {:href "/assets/paper/paper.pdf" :class "btn btn-md btn-default"} "View our Apps"]
           ]
          
       ]
       ]]

    [:script " var sliders = {}; var parcoords = null; var gridster; var papers = []; var selectedDiv = null; var selectedPaper = null;"]
    [:script {:src "/js/cljs.js"}]
    [:script (str "var allData = " (all-data-json) ";")]
    [:script "
        function getSlider(id) { return sliders[id]; }

        function setSelected(div, paper) {
          selectedPaper = paper;
          selectedDiv = div;
          $('.spermdiv').attr('class','spermdiv');
          div.addClass('spermdiv-selected');
          updateManual(myospermglyph.server._getDefsForPaper(selectedDiv.attr('id')));
        };

        function selectAnimalPreset(str) {
          var dict = myospermglyph.server._drawAnimalPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
          updateManual(dict);
        };

        function selectHumanPreset(str) {
          var dict = myospermglyph.server._drawHumanPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
          updateManual(dict);
        };

        function updateManual(dict) {
          for(i in dict) {
            if(sliders[i] != null)
              sliders[i].setValue(dict[i]);
          }
        }

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
        
          $('.human-preset-link').attr('href',function(d) { return 'javascript:selectHumanPreset(\"' + $(this).attr('id') + '\");'; });
          $('.animal-preset-link').attr('href',function(d) { return 'javascript:selectAnimalPreset(\"' + $(this).attr('id') + '\");'; });

          _.each(papers, function(p) {
             $(p.canvas).css({'pointer-events': 'none'});
             $(p.canvas).parent().click(function(div) {  setSelected($(div.currentTarget),p); });
          });

          $('a[data-toggle=\"tab\"]').on('shown.bs.tab', function (e) {
              if(e.target.hash == '#explore') {
                parcoords = createSpermPC(allData, '#explore-pc', '#explore-grid', [550,600], 'group',
                  ['img','name','desc','note']);
                parcoords.render();
              }
              else {
                
              }
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



