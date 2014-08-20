(ns myospermglyph.server
  (:require
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [clojure.math.numeric-tower :as math]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [ring.adapter.jetty :as ring]
            [clojure.walk :as walk]
            [clojure.algo.generic.functor :as gen]
            [clojure.java.jdbc :as sql]
            [myospermglyph.model :as model]
    )
  (:use [hiccup.core]
        [compojure.core])
  (:gen-class)
  )

;
; /
;

(defn main-layout [& content]
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
           [:link {:href "http://fonts.googleapis.com/css?family=Open+Sans" :rel "stylesheet" :type "text/css"}]
      [:body content]))

(defn create-navbar []
  (html
    [:div {:class "navbar navbar-inverse navbar-fixed-top" :role "navigation"}
      [:div {:class "container"}
        [:div {:class "navbar-header"}
          [:button {:type "button" :class "navbar-toggle" :data-toggle "collapse" :data-target ".navbar-collapse"}
            [:span {:class "sr-only"} "Toggle navigation"]]
            [:a {:class "navbar-brand" :href "/"}[:img {:src "/assets/img/logo_logo.png" :class "logo-top"}]]
        ]
        [:div {:class "collapse navbar-collapse"}
          [:ul {:class "nav navbar-right"}
            [:a {:href "http://github.com" :alt "View our code on Github!"}[:i {:class "fa fa-github-square github-top"}]]
          ] 
         ]]]))

(defn main-content []
  (main-layout
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
        [:div {:class "logo-container"} [:img {:src "/assets/img/logo_logo.png" :class "logo" }]]
        [:div {:class "blurb"} "Glyph-Based Video Visualization for Semen Analysis"
          [:p {:class "blurb"} "Brian Duffy, Jeyarajan Thiyagalingam, Simon Walton, David Smith, Anne Trefethen, Jackson C. Kirkman-Brown, Eamonn A. Gaffney and Min Chen"]]
        [:div {:class "blurb"} [:a {:href "/try#zoo" :class "btn btn-md btn-default btn-enter"} "Enter SpermZoo!"]]
       ; bottom content
       [:div {:class "footer clearfix"} 
       [:div {:class "footer-inner"} 
        [:div {:class "row clearfix"}
          [:div {:class "inner cover col-md-4"}
           [:img {:src "/assets/img/logo_icon-footer.png" :class "footer-img"}] [:h2 {:class "cover-heading"} "What is it?"]
            [:p {:class "lead"} "We have devised a glyph design encoding 20 numerical measurements of a sperm cell to summarize its complex spatiotemporal motion characteristics. Do you want to see?"]
              [:a {:href "/try" :class "btn btn-md btn-default"}"Make your own Glyph!"] 
           ]
          [:div {:class "inner cover col-md-4"}
           [:img {:src "/assets/img/logo_icon-footer.png" :class "footer-img"}]
           [:h2 {:class "cover-heading"} "Our Paper"]
            [:p {:class "lead"} "For more information on our technique, including how the attributes of the sperm cell were defined and encoded, please see our TVCG paper to be presented at " [:a {:href "http://ieeevis.org"}"IEEE VIS 2014"] " in Paris."]
              [:a {:href "/assets/paper/tvcg.pdf" :class "btn btn-md btn-default"}"Read the PDF"] "&nbsp; &nbsp;"
              [:a {:href "/assets/paper/semen-glyph.bib" :class "btn btn-md btn-default"}"Get the Bibtex"]
          ]
          [:div {:class "inner cover col-md-4"}
           [:img {:src "/assets/img/logo_icon-footer.png" :class "footer-img"}]
           [:h2 {:class "cover-heading"} "Who are we?"]
            [:p {:class "lead"} "Good question! We are the Oxford Visual Informatics Lab (aka Ovii) at Oxford University's " [:a {:href "http://www.oerc.ox.ac.uk/"} "e-Research Centre (OeRC)"], " and we are led by Professor Min Chen. Our main page is at " [:a {:href "http://ovii.org"}"ovii.org"] "."] 
              [:a {:href "http://www.ovii.org/" :class "btn btn-md btn-default"} "View our Apps"]
           ]
       ]
       ]]
       ]]))

;
; /try
;

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
           [:link {:href "http://fonts.googleapis.com/css?family=Open+Sans" :rel "stylesheet" :type "text/css"}]
      [:body content]))


(defn create-slider[s]
  (let [step (get s :step 1.0)
        txt-id (str (:id s) "-text")
        ]
  (html
      [:div {:class "form-group"}
        [:label {:for (:id s) :class "control-label"} (:name s)] [:span {:class "control-desc"} (str " " (:desc s))]
        [:div {:style "width:100%" :class "row form-group"} 
          [:div {:class "col-sm-9"} [:input {:type "text" :data-slider-min (str (:min s)) :data-slider-step (str step) :data-slider-max (str (:max s)) :id (:id s) :class "create-slider"}] ]
          [:div {:class "col-sm-3"} [:input {:type "text" :class "form-control input-manual-textbox" :id txt-id}] ]
           ]]
      )))

(defn create-slider-group[id nicename desc sliders]
  (html
      [:div {:class (str "bs-callout bs-callout-" id)}
       [:img {:src "/assets/img/logo_icon-footer.png"}][:div [:h4 nicename] [:div desc]]
          [:div {:class "form-horizontal"}
            (map (fn [x] (create-slider x)) sliders)
        ]]))

(defn create-thumb[id img title desc]
  (html
    [:div {:class "col-xs-6 col-md-3 zoo-thumbnail-container"}
        [:div {:class "thumbnail"}
          [:a {:title desc :id id :class "animal-preset-link" :href (str "javascript:selectZooPreset('" img "');")} [:img {:src (str "/assets/img/species/" img) :alt desc}]]
          [:div {:class "caption"} title]
;              [:button {:type "button" :class "btn btn-primary btn-sm btn-zoo"}
 ;               [:span {:class "glyphicon glyphicon-eye-open"}] (str " " title)
  ;            ]
            ]]))

(defn create-human-preset[ids, img, title, desc, note]
  (let [img (if (not (string/blank? img)) img "logo_default.png")]
   (html
    [:div {:class "media sperm-preset-media-box" }
      [:a {:class "pull-left human-preset-link" :href "#"}[:img {:class "media-object" :src (str "/assets/img/human/" img) :alt title}]]
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

(defn create-user-submitted-browser []
  (html [:div {:id "submitted-grid"}
       ;   (html (map-indexed (fn [i x] (html [:div {:class "user-submitted-result-box" :id (str "usrb-" i)}])) (range 0 100)))
         ]))

(defn create-human-presets []
  (let [rows (get-human-data)
        rows (doall (map (fn [k] (create-human-preset (first k) (:img (second k)) (:name (second k)) (:desc (second k)) (:note (second k)) )) rows))]
    (html [:div {:class "media"} [:div {:class "media"} rows ]])))

(defn all-data []
  (map (fn [k] (merge (second k) {:id (first k)} ))
    (merge 
      (gen/fmap (fn [x] (assoc x :subtitle "(human)")) (get-human-data))
      (gen/fmap (fn [x] (assoc x :subtitle "(animal)")) (get-zoo-data)))))

(defn all-data-json [] 
  (json/write-str (all-data))) 

(defn create-pc-grid []
  (html (map (fn [x] (html [:div {:class "pull-left pc-result-box" :id (str "pc-result-box-" (name (:id x)))}])) (all-data))))

(defn create-persist-viewer-modal [id]
  (let [params (model/grab id)]
    (html 
      [:div {:class "modal fade" :id "persist-modal" :role "dialog" :aria-hidden "true"}
        [:div {:class "modal-dialog modal-lg"}
          [:div {:class "modal-content"}
            [:div {:id "persist-modal-intro" }]
            [:div {:id "persist-modal-inner" :data (if (nil? params) "" params) }]
            [:div {:id "persist-modal-outro" }]
          ]
        ]
      ])))

(defn view-content [spermid]
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
    (create-persist-viewer-modal spermid)

    [:div {:class "container-outer container"}
      [:div {:class "container container-main"}
        [:div {:class "row"}
            ; left-hand col
          [:div {:class "col-md-6"}
            [:div {:class "logo-container-header"} [:img {:src "/assets/img/logo_logo.png" :class "logo-small" }]
              [:a {:href "#" :id "schematic-popover" :class "pull-right"
                    :data-toggle "popover" :data-placement "bottom" :data-content "<img width=\"600\" src=\"/assets/img/schematic.png\"/>"} 
                [:i {:class "fa fa-eye"}] " View Glyph Explanation"
             ]]
             [:div {:id "left-grid" :class "gridster spermgrid"}
              [:div {:class "alert alert-info alert-dismissible"  :role "alert"}
               [:button {:type "button" :class "btn pull-right" :id "dismiss-instructions" :data-dismiss "alert"}
                [:span {:aria-hidden "true"}[:i {:class "fa fa-times-circle"}] " Got it!"][:span {:class "sr-only"} "Close"]]
                  [:p "Select a glyph below "[:i {:class "fa fa-arrow-circle-down"}] " and modify to the right " [:i {:class "fa fa-arrow-circle-right"}] ". "]
                  [:p "There are multiple glyphs so that you can compare different parameter combinations. You can drag them around and resize them, too!"]
              ]
              [:ul
                [:li {:data-row 1 :data-col 1 :data-sizex 2 :data-sizey 2}[:div {:class "spermdiv spermdiv-selected" :id "spermbig"}]]
                [:li {:data-row 1 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall0"}]]
                [:li {:data-row 2 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall1"}]]
                [:li {:data-row 3 :data-col 1 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall2"}]]
                [:li {:data-row 3 :data-col 2 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall3"}]]
                [:li {:data-row 3 :data-col 3 :data-sizex 1 :data-sizey 1}[:div {:class "spermdiv" :id "spermsmall4"}]]
              ] 
            ]
            [:div {:id "left-explore" :style "display:none"}
              [:div {:id "left-explore-grid"} 
                  (create-pc-grid) 
               ]
             ]
           ]
         ; right-hand col
          [:div {:class "col-md-6 right-controls"}
            [:ul {:class "nav nav-tabs" :id "main-tabs"}
              [:li  [:a {:href "#zoo" :data-toggle "tab"}[:i {:class "fa fa-paw"}] " Animal"]]
              [:li [:a {:href "#human" :data-toggle "tab"}[:i {:class "fa fa-child"}] " Human"]]
              [:li [:a {:href "#explore" :data-toggle "tab"}[:i {:class "fa fa-eye"}] " Explore"]]
              [:li  {:class "active"}[:a {:href "#manual" :data-toggle "tab"}[:i {:class "fa fa-cogs"}] " Create"]]
              [:li [:a {:href "#submitted" :data-toggle "tab"}[:i {:class "fa fa-users"}] " User-submitted"]]]
            [:div {:class "tab-content"}
            ; manual
              [:div {:class "fade in tab-pane active" :id "manual"} 
                [:div {:class "row"}
                  [:div {:class "col-sm-6"}
                    (create-slider-group "kinematics" "Kinematics" "Movement of the head"
                      [{:id "vsl" :name "VSL" :desc "Straight-line Velocity, &micro;<i>m</i>/s" :min 20 :max 400}
                       {:id "vap" :name "VAP" :desc "Average Path Velocity, &micro;<i>m</i>/s" :min 20 :max 400}
                       {:id "vcl" :name "VCL" :desc "Curvilinear Velocity, &micro;<i>m</i>/s" :min 20 :max 400}
                       {:id "bcf" :name "BCF" :desc "Beat Cross Frequency <i>Hz</i>" :min 0 :max 50}
                       {:id "alh" :name "ALH" :desc "Amp. of Lateral Head Disp. &micro;<i>m</i>" :min 0 :max 50}
                       {:id "mad" :name "MAD" :desc "Mean Anglular Displacement, &deg;" :min 0 :max 60}])
                    (create-slider-group "uncertainty" "Uncertainty" "Machine vision uncertainty"
                      [{:id "headuncertainty" :name "Head" :desc "In capturing the head" :min 0 :max 1 :step 0.01}
                      {:id "uf" :name "Flagella" :desc "In capturing the flagella" :min 0 :max 1 :step 0.01}])
                  ]
                  [:div {:class "col-sm-6"}
                    (create-slider-group "mechanics" "Mechanics" "Mechanics of the flagella"
                      [{:id "fta" :name "FTA" :desc "Total Projected Arclength, &micro;<i>m</i>" :min 20 :max 400}
                       {:id "ftc" :name "FTC" :desc "Change in Angle, &deg;" :min 0 :max 60}
                       {:id "ftt" :name "FTT" :desc "Total Torque, <i>N</i>&micro;" :min 0 :max 1 :step 0.05}
                       {:id "fas" :name "FAS" :desc "Asymmetry" :min -1 :max 1 :step 0.05}])
                     (create-slider-group "morphological" "Morphological" "Head characteristics"
                      [{:id "headlength" :name "Length" :desc "The length of the head" :min 1 :max 7 :step 0.25} 
                       {:id "headwidth" :name "Width" :desc "The width of the head" :min 1 :max 7 :step 0.25}
                       {:id "headangle" :name "Rotation" :desc "The head's rotation, &deg" :min -50 :max 50}])
                     ]
                  ]
                ; share
                [:div {:class "row"}
                  [:div {:id "div-share"}
                 [:div {:class "col-sm-1"} [:h3 [:i {:class "fa fa-share-alt"}]]]
                 [:div {:class "col-sm-11"}
                    [:div {:class "row" } [:h4 "Share your creation!"]
                      [:div {:class "col-md-6"}
                       [:div {:style "width:250px"} [:input { :class "pull-left form-control input-sm sperm-name-input"  :id "name" :placeholder "Name me!"}]]
                       [:div {:style "width:250px"} [:textarea {:rows "5" :class "pull-left form-control input-sm sperm-name-input"  :id "description" :placeholder "Describe me!"}]]
                       ]
                      [:div {:class "col-md-6"}
                        [:div {:id "where-to-share"} "Where to share?" ]
                      [:a {:href "javascript:void(0);" :id "shar-manual" :class "shar" }[:i {:class "fa fa-share"}] " Manual"]
                      [:a {:href "javascript:void(0);" :id "shar-twitter" :class "shar" }[:i {:class "fa fa-twitter"}] " Twitter"]
                      [:a {:href "javascript:void(0);" :id "shar-facebook" :class "shar" }[:i {:class "fa fa-facebook"}] " Facebook"]
                     ]
                    ]
                   ]
                  ]
                 ]
                ]
            ; zoo
              [:div {:class "fade tab-pane zoo-container" :id "zoo"} [:h2 "Animal Presets"] [:p "Click an animal to see its sperm glyph! Scroll down for more animals."  (create-zoo) ]]
            ; human presets
              [:div {:class "fade tab-pane zoo-container" :id "human"} [:h2 "Human Presets"] [:p "Click an item to see its the sperm glyph for a human sperm in that category."  (create-human-presets) ]]
            ; submitted
             [:div {:class "fade tab-pane" :id "submitted"} [:h2 "User-submitted"] [:p "Check out these user-submitted entries! You can submit your own using the <i>share</i> function in the <i>manual</i> tab. <p>Hover over an item to see its description, or click an item to send it to the selected cell on the left.</p>" (create-user-submitted-browser) ]]
             ; filter
              [:div {:class "fade tab-pane" :id "explore"}
                [:div {:style "height:80px"}
                 [:h2 {:class "pull-left"} "Explore Dimensions"]
                 [:button {:type "button" :id "reset-brushes" :class "pull-right btn btn-default"} "Reset Brushes"]]
                [:div {:class "clearfix"} 
                  [:p "Each axis in the plot is a dimension of the cell data. You can brush to filter the sperm matching those properties, which will appear to the left."]
                  [:div {:id "explore-pc" :class "parcoords"}]
                ]
             ]
          ]]]]]

    [:script " var sliders = {}; var parcoords = null; var gridster; var papers = []; var selectedDiv = null; var selectedPaper = null;"]
    [:script {:src "/auto-js/cljs.js"}]
    [:script (str "var allData = " (all-data-json) ";")]
    [:script {:src "/assets/js/client.js"}]
  ))

(defroutes main-routes
  (GET "/" [] (main-content))
  (GET "/load/:id" [id] (view-content (str id)))
  (GET "/try" [] (view-content -1))
  (GET "/usersubmitted/:n" [n] (model/grab-latest n))
  (GET "/persist" [obj] (str (model/create (json/read-str (str obj) :key-fn keyword))))
  (route/resources "/"))

(def app 
  (handler/site main-routes))

(defn start [port]
  (ring/run-jetty app {:port port
                               :join? false}))

(defn -main []
  (model/migrate)
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))

