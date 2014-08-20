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

(defn photo-credit-content []
  (view-layout
    (create-navbar)
     [:div {:class "container-outer container"}
      [:div {:class "container container-main"}
        [:h2 "Photo credits"]
          [:p "Many thanks to the below authors of the photographs that we have used for choosing the Creative Commons licence!"]
          [:div 
           [:ul
            [:li [:a {:href "https://www.flickr.com/photos/alexk100/1091960123/in/photolist-2EuzGF-avmPYg-4HbvPV-2oWyph-93dGMM-4ZYNsa-9GEifh-7ZtsDu-8yE1Ly-6dCAah-dhCvsS-6zQUfM-83qWSM-6jku6x-4jdYwf-38eLr9-bNAUNM-7X6NY5-7TuUJf-7Qwun5-5UvkDu-5EyD46-5iEqjU-3GVH4U-2aatJv-nd3QA-iYmG4-4E6nj-anUAWz-8hLqXn-8dxbfZ-4rojL7-aEBHHG-dH73ji-6PwgKU-3GRr88-bFvzov-8ksyBs-8dArQj-9N4WDW-7E1mEg-dEFZRb-as9egM-8rNjkk-8rCoNh-7Cxxkd-4DtgxE-4Lbxc6-4sTRff-4r3mbP"} "Rat "]]
            [:li [:a {:href "https://www.flickr.com/photos/found_drama/5347954220/in/photolist-bkD2gj-bkD1Ty-99zEtf-bywbmD-bkBhBE-6R36kY-5r2efR-5WwhEQ-5r6yPA-7gkSn-7gkS9-3cfaD1-byo5LX-4BDkou-7GZe9d-bktcbu-byo5Qi-5Ymzbv-5ECoF7-3cy8pw-dX6PtV-4Bz5cc-4BDqru-53TLLS-4Bz936-53TLNq-4Bz75T-4Bz3sn-4BDopG-7gkRE-4BDq2j-8F9Cc7-4Bz5QD-69yfSS-6X2DNN-7gkSL-7FMRtS-4XQYyA-8F9CgU-6Ft3RA-37vRSF-6BMQfc-6BMPEa-6BMPj8-tqBoW-99wxpk-6XEKAg-8vuzw2-8F6sdZ-a9jc1v"} "Ram"]] 
            [:li [:a {:href "https://www.flickr.com/photos/beeldenzeggenmeer/405092064/in/photolist-BNcKW-gC6zfY-5u81E6-axJNm-a5RN62-fnyxry-Dad3R-5ypmXu-jYse-ndrgn-3pBjw-k3MXQZ-PEbBJ-cxMjY-jJnC2f-9z64C-7FEQHe-a2jGkQ-3jeTLt-cUwbe-J56AA-jLRc6p-9G31fA-kkaXDW-xWEJM-MwEZA-cP6EEC-DRn2H-4EgGJT-bseb7Z-6LRUnV-5n6hjR-W5yYx-2G1BYh-5dzMBA-LE1QN-JscF9-7ZThpJ-h1MXxk-beYFCV-apf8VN-iuL9V-h1T2h-iHtpa4-p8gvG-fn9Jc-5PxRC-a1PjXQ-bky6Zq-ehuNk1"} "Pig"]] 
            [:li [:a {:href "https://www.flickr.com/photos/voght/2441818832/in/photolist-hUUdVm-nx16nc-4x3oAi-bUvYAA-4HLXEG-6j1z4g-beoUXF-fwdxK1-dycm-fPgiZm-bRX2Lz-5XeGT8-ekxPSB-dviME1-61RBsC-5niQaf-aK2tYp-j5phFz-aiBJoy-dviuV7-9xkpWH-52GdPC-bUJo4-3QggWt-bbYnQV-nBNBPC-ea54Fs-bDPacf-ea54EU-fG2iDU-6eAucU-G59fg-dGgMHt-atzqRj-nRf1x5-moQHZi-48zxQb-7XrR1s-edfvZo-3PRGZx-2xi9Pc-4QrcDm-6s65j8-j45pb4-bzHEpz-dGdm4n-8VJj32-4sLaTR-4sQe9Q-nQo6Dr" } "Turkey" ]] 
            [:li [:a {:href "https://www.flickr.com/photos/alumroot/4714557504/in/photolist-8bBkGd-hWfVt-7yeoFk-dfW3A6-7fXM8F-5pBKxL-5yAVdS-c7RMJb-5exaaC-5esLVk-8MCK5h-9LzYXK-9LCLKY-7t3J1s-jQqVMT-49AD3r-4yUzdc-dwuzgH-arsy6d-ejSUpG-7t3J85-B5a5S-bTSyEe-9XhV-4sEhKG-7t3J9f-7t3h5f-7sYiWX-7t3J2E-4NmQyy-9k4Dfa-mLmaAU-bavDXD-6p8nWR-cERmoJ-aCBFMR-aD1cmT-4Fb5e5-4Fb4gC-4F6P7D-8ATWYk-nruYWW-9qf52x-7gLNW5-9EA2R6-8cK5eF-wpGDX-dVtmob-7gE7fu-ATaYq"} "Catfish" ]] 
            [:li [:a {:href "https://www.flickr.com/photos/calliope/25239705/in/photolist-3emTk-5s9YNj-4o9xaT-cjYb7j-by4Joi-6gqxU4-8wCP8g-4V1rdC-4mtHoN-Lno3-6Q1kjT-deTs6K-8mxZpn-8XxY35-kPQt1Y-btwhMk-2Xbzx-8mxYjZ-6NyuB4-cjU5VL-bQK5Q2-5TRYrA-3mMhMg-74qQPX-6tFZPb-8dwyDv-9zvZiy-5cUdZH-cBwbyN-kK4c8i-3xjT1t-bMKQgc-58kTUL-4KgrpH-6MLmEF-3ftipK-7yZNtK-7xUVgx-cjYrUG-2WzsE-5CqiLg-3gNG2J-4TZmfX-4TZmep-633kuG-38uQBo-dWnTVC-89ZUh8-952eDH-djxu3H/"} "Boar"]] 
            [:li [:a {:href "https://www.flickr.com/photos/highwayoflife/5755418924/in/photolist-9LA2wL-eeHFjw-nVYk3-eeBXvP-vGqbT-aCtkJt-3cBkAf-eeBXFX-FfiNQ-yML6m-4fk1Ei-d9WFp2-osqxq-3JxT8C-AmTLN-6TTJuH-feoHUA-f6sgAH-iwPHjM-eT3s-h3tNQy-iwPoL5-2h6VS-FpeB3-4pQxnT-4oSif7-5UncJw-osqxw-pSWV2-6VroA-5kZZXt-6EMBPu-6ZN2in-7gC6Q2-6ZS3LA-5fY5Ny-9oj41a-6ZS3vJ-25uxw-9AatLo-5kURx7-jdMiC-5qnKnZ-8d1rjD-gVWtrx-gXsK6a-9y6fUn-eARJN-6gmVbY-e95MQU"} "Horse"]] 
            [:li [:a {:href "https://www.flickr.com/photos/texaseagle/4445781656/in/photolist-Lf9B4-cDtzf-7LRMZS-n9ZPBn-99dwKu-8j3QSU-hHTc1-7w6sgm-82r2tv-7qWNj-3UYTXm-7LRMYG-2f5bn-8EdyA-bx3faz-k2d55-jzKBe-aYnLFT-zDe8E-4NCPSm-fBX2YS-uSxve-acxHDU-5cgjxA-5FnzF1-dG5b7k-dG58tg-fiQq77-zDejn-9FAoJu-6qGJTn-jaSKc-7n5d1A-7n1hYK-8tB2B8-7n5cyA-8Dh2Hz-4pZPRU-8Dh3Qg-nm2dWo-eSSrNw-uirDw-nBf12P-9XsG52-gNaTgX-a6v94f-8BgL9P-bAzTx8-dtZ2h-56GLE"} "Possum"]] 
            [:li [:a {:href "https://www.flickr.com/photos/ebertek/251626439/in/photolist-aCkocd-7cX24G-zwARW-8ApSAp-oeDLT-bJBKnB-N7EYz-9QB9UH-5Jdzzq-eujez2-aEDoJe-gabwM3-6fNyq2-jcmujk-9HE2Se-nse7qc-9M3htt-eSLLYL-Kti9M-MZAX4-brrB8Y-dr8i8-76X4fy-9sCvig-cy4QeL-Bpqdb-NhABH-8WQzYy-75mFk-fBYPoE-A6853-8WEyme-9g4iX6-4jTWVA-kyyGdT-aB2wjf-4oX2Kh-dQ5ni3-Kv3M6-nKpFxc-69vv9r-j7AzBY-9Ysfdt-4rZcnz-gapsk8-9F1pte-h1ULAW-48DMjc-9WXi9d-AXLTm"} "Donkey"]] 
            [:li [:a {:href "https://www.flickr.com/photos/tambako/10655212644/in/photolist-heyKuQ-hdL8oA-bzGruv-5HERR8-dXuW7u-bVYAwf-he7L1y-dXyYjd-4ZFUit-eXmEui-eBwgL-dS49U4-jzK9P-e2uZRY-aiBhgc-3qsPsy-9YPgUP-9YP6XK-dXmYq9-77shPy-9YTZk7-9YRb6c-aGVZJ2-dY33S7-Q2t4e-6xXrBN-a6BYkH-77shPL-8ib1C-8unSxH-8uqYew-Kc1cb-4cNcxT-diZNvr-4po2ej-eD7Jmi-dSAD7R-h1TUQy-h1TSKb-FwM4u-bFcRqd-dX5vCP-242bwa-4ZLaFj-98xaF2-bAnCp6-5ZKVdu-apY68-fSnfbd-5YEyVj"} "Marmoset"]] 
            [:li [:a {:href "https://www.flickr.com/photos/aigle_dore/8300920648/in/photolist-dDwovw-4qFQYF-57JMUp-fXRSo-e3uwEk-wRGt3-5nEbcc-4cwiM4-bNn56X-4VoiFG-9ZZbx7-dSek7E-dyfpmt-iErrSn-myyxmL-dj7A8M-dUtpsb-3dX91V-66CngG-avHemi-Gpjv8-e9MoAr-yjymc-bihUhn-fzPQNX-61nyT7-bRHqDi-dMA2E-hMapD3-akDPPi-2TanKc-fCDyCP-e8dWiJ-dykUiG-a1quYk-8H8YTY-nmb7a8-fJHa1z-eXoias-3KiWrp-5fR9pi-dhU9RW-sBzeE-kjqGfV-e3uxQV-fePfZD-iUdbzi-euq7N7-bm3B73-fCKgGe"} "Cat"]] 
            [:li [:a {:href "https://www.flickr.com/photos/barathieu/7277953560/in/photolist-dbbCJt-c68q2W-6R1Cn5-jBjqEz-827f8-2BsQNe-53hva2-zEm7c-fKmph3-54s7iH-9EGtBt-4iEZ1E-dBrmLG-a6cdWw-8W7dHG-eRDUyu-tifho-b9ujsD-6jv3S7-c5iLLE-fj9tJ-8vAC1Z-8htqzU-cMjwzo-7zjpbx-6Xnmcs-4GD27n-6XnkYu-4vcymx-85sfN2-zFXX5-zEm6U-768axn-7vnkhB-5Ldkr3-nAyTTr-Gbnbm-BUgqK-azLbRG-2WiuDL-52f3N-nho2bN-2ydvUs-82AeJL-54wkw7-34s9vM-BUgtN-hsk2hS-UfUc-cMjwmQ"} "Sperm Whale"]] 
            [:li [:a {:href "https://www.flickr.com/photos/justaprairieboy/3645071936/in/photolist-6y6WTS-6BeVts-52GHU2-nzp9WF-7HSxAr-jevbns-5wkqX6-7QUDTK-4GVkiy-7gCEwL-sKiLw-7HSxHp-EhcBB-6mrcuD-5ZQnfd-iUP5as-6cmcda-fFDc53-7HWtW1-j16XDA-eW4JJH-jzHtSn-9Nb955-6XHdNk-3c38WW-eQna2i-4wf4XL-7HWsZq-97EZE3-dpRXjM-74N3cj-n3NXRb-eDEryK-PVNng-bhQyiv-gfpPY-57zoFX-wNoFK-t17ev-5gmWJz-m2Sfro-ssqxX-p3At9-gEjz3y-kNDxKA-ttb83-fGxrW-4BmxLa-6Ys56L-edWDB2"} "Bull"]] 
            [:li [:a {:href "https://www.flickr.com/photos/jcapaldi/7823431946/in/photolist-cVk8KG-d9iXS-7SU1DT-9Ykavi-eYue5G-enFP3o-5j4hSz-amnKLj-jTX79-9tZ3SK-aDyVYz-4HTykM-eP7HCa-ebkzqT-6BqJiN-exYHc-gqFvnb-95RQyJ-9LhCzX-6Dwv5q-dbn3NW-cVkaPj-acoing-cUDWCd-aKnyCk-9tyuJW-2qQt8a-6WQXTF-nd3n5-7Yx4DB-eykUuk-fnCknQ-6DwuYJ-4C6GRL-Lr6tS-dbn1iX-dGhRce-fDzWF7-4Dhfad-cVkan9-cmF87o-7bxjgs-5gBd8w-fgDo9z-in3Tb-6n4qgn-nMQHQJ-6GTBSA-6EhrnF-7TWnjA"} "Rabbit"]] 
            [:li [:a {:href "https://www.flickr.com/photos/nationalzoo/8123164308/in/photolist-dnPkK3-dnPkX3-9ogMPu-5RnfKF-behKUz-fQenJV-ebDL2E-dkMQZj-8vLFXD-d9RAN5-a1fXqw-714KwL-9q6j6R-kYfasM-dfv9hP-7rDrLu-9FwWym-dsmJV6-NSrYy-a2ip5y-4fojhC-6LG5vh-dN9uJ2-6P8VAo-facm6z-73M5hi-37nqnu-cPMpWo-9qSXYs-dnPm23-dTbr99-auJJmX-BfomN-5jaLRE-dT5Pwk-mHy8V4-cRHKsE-aAiFAn-kW8J2u-awW36P-22BHuw-4rfoPe-8TtJ3G-9Ga1QP-9PBQsz-db2DvF-hZhDW4-a8CFN1-hMmza-d9RA3v"} "Gazelle"]] 
            [:li [:a {:href "https://www.flickr.com/photos/steve_chilton/1517222734/in/photolist-4VMdP5-8xz7qh-yyQ2t-6GHBF5-3aToLj-8xeBPS-8Bmn1r-7Vj2t-bTRSv-8eWaHF-3j5asE-8xz7pW-8RnjyQ-BAFSy-5cVw6A-DynS5-28tQPX-4XnMyH-dQ8RBA-YVke-bGWME-rvyu-7oQUU7-bvAJ4-6iA4xA-cXn2t-5XvDG7-7ELDmB-6BwKGu-zr7xQ-4JMvY7-5KpDDH-4Xs4bo-7EQr6L-4RLCWm-5UsUaZ-8o6Erz-6d6xDd-4bCcuT-oF4L-hKPK2-euANEX-7nzjrU-7qaagW-4skQmx-hq7Et-Fnjuf-4xuguf-6i9d32-4wNoAL"} "Hamster"]] 
            [:li [:a {:href "https://www.flickr.com/photos/21560098@N06/3796822070/in/photolist-6MvGVu-a8kWpA-4xo1ia-8zs1M8-6YsBBh-khsdmZ-7puJHj-Fu537-8ZMP7s-hynLWC-9KP8b7-CUAZd-6vBxHb-7pn5ms-5Y2Cnc-e1Xjj6-4EadG7-tZ1DL-dCNQf-9WKydL-h9AWfA-GDMz5-xDvy-5qA4PQ-7pr4fg-hvECij-7pr48p-6YYfwt-7puqnw-ekUdAu-Fu5DJ-7CAowV-9LRKw-4iitns-89Hao9-8eyS4W-hyokem-9MsbU4-8i3ovZ-2PM8Xu-iTkHQ-boNV9d-ePR74K-9FZpJ3-33aj3-4pLRT8-KUv57-7iX2Wg-2vC7E-pa6zn"} "Mouse"]] 
           ]
           ]
       ]]
    ))


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
          ]]]]]

    [:script " var sliders = {}; var parcoords = null; var gridster; var papers = []; var selectedDiv = null; var selectedPaper = null;"]
    [:script {:src "/auto-js/cljs.js"}]
    [:script (str "var allData = " (all-data-json) ";")]
    [:script {:src "/assets/js/client.js"}]
  ))

(defroutes main-routes
  (GET "/" [] (main-content))
  (GET "/photocredits" [] (photo-credit-content))
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

