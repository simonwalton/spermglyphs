(comment "
    Make Your Own Sperm Glyphs
    Copyright (C) 2014 Simon Walton

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    "
)

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
            [myospermglyph.common :as common]
            [myospermglyph.otherpages :as otherpages]
    )
  (:use [hiccup.core]
        [compojure.core])
  (:gen-class)
  )

;
; /
;


;
; /try
;


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
    [:div {:class "col-sm-3 zoo-thumbnail-container"}
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
  (common/view-layout
    [:script {:src "/assets/js/raphael-min.js"}]
    [:script {:src "/assets/js/jquery.min.js"}]
    [:script {:src "/assets/bootstrap/js/bootstrap.min.js"}]
    [:script {:src "/assets/js/bootstrap-slider.js"}]
    [:script {:src "/assets/js/underscore-min.js"}]
    [:script {:src "/assets/js/jquery.gridster.min.js"}]
    [:script {:src "/assets/js/d3.v2.js"}]
    [:script {:src "/assets/js/d3.parcoords.js"}]
    [:script {:src "/assets/js/pc-filter.js"}]
   
    (common/create-navbar)
    (create-persist-viewer-modal spermid)

    [:div {:class "container"}
        [:div {:class "row"}
            ; left-hand col
          [:div {:class "col-sm-6"}
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
          [:div {:class "col-sm-6 right-controls"}
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
                   [:div {:id "param-link-indicator"} 
                      [:span {:class "fa fa-link" :title "Note that VSL < VAP < VCL must hold." :alt "TEST"} " "]
                      [:div {:id "param-link-mid"} ]
                     ]
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
                      [:div {:class "col-sm-6"}
                       [:div {:style "width:250px"} [:input { :class "pull-left form-control input-sm sperm-name-input"  :id "name" :placeholder "Name me!"}]]
                       [:div {:style "width:250px"} [:textarea {:rows "5" :class "pull-left form-control input-sm sperm-name-input"  :id "description" :placeholder "Describe me!"}]]
                       ]
                      [:div {:class "col-sm-6"}
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
              [:div {:class "fade tab-pane zoo-container" :id "zoo"} [:h2 "Animal Presets"] [:p "Click an animal to see its sperm glyph! Scroll down for more animals. 
                  <small><a href=\"/photocredits\">(photo credits)</a></small>" (create-zoo) ]]
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
          ]]]]

    [:script " var sliders = {}; var parcoords = null; var gridster; var papers = []; var selectedDiv = null; var selectedPaper = null;"]
    [:script {:src "/auto-js/cljs.js"}]
    [:script (str "var allData = " (all-data-json) ";")]
    [:script {:src "/assets/js/client.js"}]
  ))

(defroutes main-routes
  (GET "/" [] (otherpages/entrance-content))
  (GET "/photocredits" [] (otherpages/photo-credit-content))
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
  (println (str "You passed in this value: "))  
  (model/migrate)
  (let [port (Integer. (or (System/getenv "PORT") "8090"))]
    (start port)))

