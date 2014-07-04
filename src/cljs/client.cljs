(ns myospermglyph.server
    (:require [jayq.core :as jq] [clojure.walk :as walk])
    (:use [jayq.core :only [$]])
)

(def origin {:x 280 :y 260})

(def globals {:cscale 3.00
              :cbase 350.0
              :hscale 4.00
              :tscale 1.50}) ; /fscale

(def currsperm {:name "Human"
          :vcl 205.26
          :vap 128.54
          :vsl 77.4
          :bcf 30.96
          :alh 47.12
          :mad 45
          :headlength 8.27
          :headangle 0.0
          :headwidth 3.65
          :headuncertainty 0.4
          :fta 50.0
          :ftc 23.0 ;/ fca
          :ftt 0.87
          :fas -0.1
          }) 
 
; global state
(def presets (atom {}))

(def defs-for-paper (atom {}))
(defn set-defs-for-paper [paper defs] (swap! defs-for-paper assoc paper defs))

  ; raphael utilities
(def paper-stack (atom {}))
(defn add-to-paper-stack [id paper] (swap! paper-stack assoc id paper))


(def colours {:nouncertainty {:red 0.32941176, :green 0.32941176, :blue 0.84705882 }})
(def colourmaps {:uncertainty {:red [0.804 1.0 0.549] :green [1.0 0.59 0.0] :blue [0.8 0.18 0.0]}})

(defn raphaelcolour [colour]
  (.getRGB js/Raphael (format "rgb(%d,%d,%d)" 
      (int (* 255 (:red colour))) (int (* 255 (:green colour))) (int (* 255 (:blue colour))))
  )
) 

(defn lerp [a b t]
  (+ a (* (- b a) t )))

(defn deg-to-rad [d] (* d 0.0174532925))

(defn sample-colourmap [cm t]
  (let [idx (* t (- (count (:red cm)) 1.0))
        a (js/Math.floor idx)
        b (js/Math.ceil idx)
        rm (- idx a)
        r (lerp (get (:red cm) a) (get (:red cm) b) rm)
        g (lerp (get (:green cm) a) (get (:green cm) b ) rm)
        b (lerp (get (:blue cm) a) (get (:blue cm) b ) rm)
        ]
  (-> (raphaelcolour {:red r :green g :blue b}))))

; general drawing 

(defn- attr [object attributes]
  (.attr object (clj->js attributes)))

(defn create-head [paper sperm]
  (-> (.ellipse paper (:x (:origin sperm)) (:y (:origin sperm))
              (/ (* (:headwidth (:params sperm)) (:hscale (:scales sperm))) (:cscale (:scales sperm)))
              (/ (* (:headlength (:params sperm)) (:hscale (:scales sperm))) (:cscale (:scales sperm))))
      (attr {:stroke "black", :fill (raphaelcolour (:nouncertainty colours)), :stroke-width 1})
      (.transform (format "r%.2f" (:headangle (:params sperm))))
  ))

(defn create-ring [paper sperm value]
  (let [radius (/ (+ (:cbase (:scales sperm)) value) (:cscale (:scales sperm)))]
    (-> (.path paper (format "M%.5f,%.5f m%.5f,%.5f a%.5f,%.5f %.5f %d,%d %.5f,%.5f" (:x (:origin sperm)) (:y (:origin sperm)) 0 (- radius) radius radius 0 1 1 radius radius ))
        (attr {:stroke "black", :fill "none", :stroke-width 1})
        (.transform (format "t%d,%dr-45" (- radius) radius))
  )))

(defn create-filled-ring [paper sperm colour from to]
  (let  [ra (/ (+ (:cbase (:scales sperm)) from) (:cscale (:scales sperm)))
        rb (/ (+ (:cbase (:scales sperm)) to) (:cscale (:scales sperm)))]
    (-> (.path paper (format "M%.5f,%.5f   m%.5f,%.5f          v%.5f         a%.5f,%.5f    %.5f     %d,%d    %.5f,%.5f   h%.5f   Z  "                            
                             (:x (:origin sperm)) (:y (:origin sperm))   0 (- ra)   (- (- rb ra))    rb rb      0      1 0     rb rb   (- (- rb ra) )))
        (attr {:stroke "none", :fill colour, :stroke-width 1})
        (.transform "r135")
  )))

(defn create-filled-pie-slice [paper sperm rada radb angle offset rotation]
  (let  [ang (deg-to-rad (- 90 angle ))
         ra (float rada)
         rb (float radb)
         hrb (* 0.5 rb)
         rbra (- rb ra)
         large-arc-flag (if (>= angle 180.0) 1 0)
         firstsnap [ (* rb (js/Math.cos ang)) (- (* rb (js/Math.sin ang)))  ]
         secondsnap [ (* ra (js/Math.cos ang)) (- (* ra (js/Math.sin ang))) ]
         thirdsnap [ (* ra (js/Math.cos (deg-to-rad (+ 90.0)))) (- (* ra (js/Math.sin (deg-to-rad (+ 90.0))))) ]
         ]
      (->(.path paper (format "M%.5f,%.5f     m0,%.5f        v%.5f     a%.5f,%.5f    %.5f     %d,%d                %.5f,%.5f    l%.5f,%.5f    a%.5f,%.5f %.5f %d,%d %.5f,%.5f  z"                       
                             (:x (:origin sperm)) (:y (:origin sperm))   (- ra)   (- rbra)    rb rb      0     large-arc-flag 1  (get firstsnap 0) (+ (get firstsnap 1) rb) 
                             (- (get secondsnap 0) (get firstsnap 0))  (- (get secondsnap 1) (get firstsnap 1)  )
                             ra ra 0 large-arc-flag 0 (- (get thirdsnap 0) (get secondsnap 0))  (- (get thirdsnap 1) (get secondsnap 1) ))) ; second arc                       
        (.transform (format "R%.5f %.5f,%.5f T%.5f,%.5f" rotation (:x (:origin sperm)) (:y (:origin sperm)) (get offset 0) (get offset 1) ) )
        )))

; semen-specific
(defn create-interior-coloured-arc [paper sperm]
  (-> (create-filled-ring paper sperm (sample-colourmap (:uncertainty colourmaps) (:headuncertainty (:params sperm))) 0 (:vsl (:params sperm)))))

(defn create-vcl [paper sperm]
  (-> (create-ring paper sperm (:vcl (:params sperm)))))

(defn create-vsl [paper sperm]
  (-> (create-ring paper sperm (:vsl (:params sperm)))))

(defn create-vap [paper sperm]
  (-> (create-ring paper sperm (:vap (:params sperm)))))

(defn create-orientation-arrow [paper sperm]
  (let [radius (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm))) (:cscale (:scales sperm)))]
    (-> (.path paper (format "M%.5f,%.5f m%.5f,%.5f l%.5f,%.5f l%.5f,%.5f z " (:x (:origin sperm)) (:y (:origin sperm)) (- 10) (- radius) 10 (- 15) 10 15))
        (attr {:stroke "none", :fill "black" })
        )))
 
(defn create-mad [paper sperm]
  (-> (create-filled-pie-slice paper sperm 0 (/ (:cbase (:scales sperm)) (:cscale (:scales sperm))) (:mad (:params sperm)) [0 0] (- (* 0.5 (:mad (:params sperm)))))
        (attr {:stroke "#666", :fill "white"})))
      
(defn create-arclength-tail [paper sperm]
  (let [zeroring (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))
        changeinangle (* (:ftc (:params sperm)) 16.0)
        flaglength (/ (* (:fta (:params sperm)) (:tscale (:scales sperm))) (:cscale (:scales sperm)))
        ]
    (-> (create-filled-pie-slice paper sperm 0 (:fta (:params sperm)) flaglength [0 zeroring] (- (+ 180 (* 0.5 flaglength))))
      (attr {:stroke "#666", :fill "#ccc"}))))
 
(defn create-fas [paper sperm]
  (let [radius (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))
        k (+ (int (/ (:fta (:params sperm)) 50.0)) 1)
        dk (/ (* 50.0 (:tscale (:scales sperm))) (:cscale (:scales sperm)))
        asymm-length (* k dk)
        ang (deg-to-rad (:fas (:params sperm)))
        r (* (* (/ (:cbase (:scales sperm)) (:cscale (:scales sperm))) 0.05) (:tscale (:scales sperm)))
        s (.set paper)
        ]
    (-> s (.push
          ; line 
          (-> (.path paper (format "M0,%.5fv%.5f" 0 asymm-length))
              (attr {:stroke "#000", :stroke-width 5}))
          )
       ; (.push (apply (fn [i] (-> (.ellipse paper 0 (* i dk) 15.0 15.0) (attr {:fill "#f00"}))) [0 1 2 3 4 5]))
      )
      (doseq [i [0 1 2 3]] (-> s (.push (-> (.ellipse paper 0 (* i dk) r r) (attr {:fill "#f00"})))))
      (-> s (.transform (format "R%.2f %.2f %.2f T%.2f,%.2f" ang 0 0 (:x (:origin sperm)) (+ radius (:y (:origin sperm) )))))
  ))

(defn create-bcf-ring [paper sperm] 
  (let [r (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm))) (:cscale (:scales sperm)))
        r2 (/ (+ (:cbase (:scales sperm)) (+ (:vcl (:params sperm))) 70) (:cscale (:scales sperm)))
        ang (* (:bcf (:params sperm)) 5.0)
        ]
    (-> (create-filled-pie-slice paper sperm r r2 ang [0 0] 225)
      (attr {:stroke "none", :fill "#ccc"}))))

(defn create-inner [paper sperm]
  (let [radius (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))]
    (-> (.circle paper (:x (:origin sperm)) (:y (:origin sperm)) radius radius)
        (attr {:stroke "#666", :stroke-width 1, :fill "#ccc"})
        )))

(defn create-label [paper sperm]
  (-> (.text paper (:x (:origin sperm)) (- (second (:size sperm)) 20) (:name (:params sperm)))
      (attr {:font-size 12, :font-weight "bold", :text-anchor "centre",  :fill "#EA553C"})))
; entry

(defn draw [sperm]
  (let [id (.attr (:div sperm) "id")
        w (first (:size sperm))
        h (second (:size sperm))
        paper (if (contains? @paper-stack id)
                (id @paper-stack)
                (js/Raphael id w h))
        sperm (assoc sperm 
            :origin {:x (* 0.5 w) :y (* 0.5 h)}
            :scales (assoc globals :cscale (* 200.0 (/ 10.0 w)))
        )]
    (-> (add-to-paper-stack id paper))
    (-> (set-defs-for-paper id (:params sperm)))
    (-> (jq/bind ($ paper) :click (fn [evn] (js/alert "click"))))
    (-> (.clear paper))
    (-> (.setSize paper w h))
    (-> (create-interior-coloured-arc paper sperm))
    (-> (create-inner paper sperm))
    (-> (create-mad paper sperm))
    (-> (create-head paper sperm))
    (-> (create-bcf-ring paper sperm))
    (-> (create-vcl paper sperm))
    (-> (create-vsl paper sperm))
    (-> (create-vap paper sperm))
    (-> (create-arclength-tail paper sperm))
    (-> (create-orientation-arrow paper sperm))
    (-> (create-fas paper sperm))
    (-> (create-label paper sperm))
    (-> (clj->js (id @paper-stack)))
  ))

(defn get-and-store-preset [url id]
  (let [keyid (keyword id)
    url (str url id ".json")]
  (->(jq/ajax {
      :url url
      :type :get 
      :success (fn [data text status] (
              (swap! presets assoc keyid (walk/keywordize-keys (js->clj data :keywordize-keys true)))
              ))
      :error (fn [data text status] (js/console.log (str "There was a problem getting " id ".json: "  text)))
      :processData false
      :contentType "application/json"
      }))))

(defn get-and-store-presets [resourceurl]
  (get-and-store-preset resourceurl "human")
  (get-and-store-preset resourceurl "animal"))

(defn init [resourceurl]
  (get-and-store-presets resourceurl))

(defn updateManual [props]
  (draw (assoc currsperm
       :vcl (.getValue (js/getSlider "vcl"))
       :vap (.getValue (js/getSlider "vap"))
       :vsl (.getValue (js/getSlider "vsl"))
       :bcf (.getValue (js/getSlider "bcf"))
       )))

; exposed functionality

(defn ^:export _getDefsForPaper [paper] (clj->js (paper @defs-for-paper)))

(defn ^:export _draw [div, size]
  (let [sperm {:div div :size size :origin origin :scales globals :params currsperm}]
    (draw sperm)))

(defn ^:export _drawManual [div size props]
  (let [sperm {:div div :size size :origin origin :scales globals :params (walk/keywordize-keys (js->clj props :keywordize-keys true))}]
    (draw sperm)))

(defn ^:export _drawHumanPreset [div id size]
  (let [id (keyword (str id))
      params (id(:human @presets))
     sperm {:div div :size size :origin origin :scales globals :params params}]
    (draw sperm)
    (-> (clj->js params))
    ))

(defn ^:export _drawAnimalPreset [div id size]
 (let [id (keyword (str id))
      params (id (:animal @presets))
     sperm {:div div :size size :origin origin :scales globals :params params}]
    (draw sperm)
    (-> (clj->js params))
   ))

(defn ^:export _drawParams [div params size]
 (let [sperm {:div div :size size :origin origin :scales globals :params (js->clj params :keywordize-keys true)}]
   (->(draw sperm))))

  ; (let [sperm currsperm
  ;   sperm (merge (get-preset "human" id) sperm)
  ;   sperm (assoc sperm :div div :size size :origin origin :scales globals)]
  ;   (-> (js/console.log "plok"))
  ;   (-> (js/console.log (clj->js sperm)))
  ;   (-> (draw sperm))))

(defn ^:export _init [resourceurl] (init resourceurl))

; jq


