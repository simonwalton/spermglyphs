(ns myospermglyph.server
    (:require [jayq.core :as jq] [clojure.walk :as walk] [goog.string :as gstring]  [goog.string.format] )
    (:use [jayq.core :only [$ css html ajax-m]])
    (:use-macros [jayq.macros :only [let-ajax]])
)
 
(def origin {:x 280 :y 260})

(def globals {:cscale 1.00
              :cbase 350.0
              :hscale 8.00
              :tscale 1.50}) ; /fscale

(def defaultsperm {:name "Blank Sperm"
          :vcl 205.26
          :vap 128.54
          :vsl 77.4
          :bcf 30.96
          :alh 47.12
          :mad 45
          :uf 0.5
          :headlength 8.27
          :headangle 0.0
          :headwidth 3.65
          :headuncertainty 0.4
          :fta 50.0
          :ftc 23.0 ;/ fca
          :ftt 0.87
          :fas -0.1
          }) 

(def currsperm {:name "Human"
          :vcl 205.26
          :vap 128.54
          :vsl 77.4
          :bcf 30.96
          :alh 47.12
          :mad 45
          :uf 0.5
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
(defn set-defs-for-paper [paper-id defs] (swap! defs-for-paper assoc (keyword paper-id) defs))
(defn get-defs-for-paper [paper-id] ((keyword paper-id) @defs-for-paper)) 

(def paper-stack (atom {}))
(defn add-to-paper-stack [id paper] (swap! paper-stack assoc (keyword id) paper))
(defn get-from-paper-stack [id] ((keyword id) @paper-stack))

  ; raphael utilities
(def colours {:nouncertainty {:red 0.32941176, :green 0.32941176, :blue 0.84705882 }})
(def colourmaps {:uncertainty {:red [0.804 1.0 0.549] :green [1.0 0.59 0.0] :blue [0.8 0.18 0.0]}})

(defn raphaelcolour [colour]
     (.getRGB js/Raphael (gstring/format "rgb(%d,%d,%d)" 
      (int (* 255 (:red colour))) (int (* 255 (:green colour))) (int (* 255 (:blue colour))))
  )
) 

; push to a given raphaeljs set a bunch of raphael objects (resulting in the set)
(defn push-to-set [rset robjs]
  (reduce #(.push %1 %2) rset robjs))

; linear interpolation
(defn lerp [a b t]
  (+ a (* (- b a) t )))

; degrees to radians
(defn deg-to-rad [d] (* d 0.0174532925))

; take a colourmap (see above) and sample at a normalised position to return a raphaeljs colour
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

; nicer way of specifying raphael attributes 
(defn- attr [object attributes]
  (.attr object (clj->js attributes)))

; cell head
(defn create-head [paper sperm]
  (-> (.ellipse paper (:x (:origin sperm)) (:y (:origin sperm))
              (/ (* (:headwidth (:params sperm)) (:hscale (:scales sperm))) (:cscale (:scales sperm)))
              (/ (* (:headlength (:params sperm)) (:hscale (:scales sperm))) (:cscale (:scales sperm))))
      (attr {:stroke "black", :fill (raphaelcolour (:nouncertainty colours)), :stroke-width 1})
      (.transform (gstring/format "r%.2f" (:headangle (:params sperm))))
  ))
 
; general cell ring given an attribute value (e.g. VCL value)
(defn create-ring [paper sperm value]
  (let [radius (/ (+ (:cbase (:scales sperm)) value) (:cscale (:scales sperm)))]
    (-> (.path paper (gstring/format "M%.5f,%.5f m%.5f,%.5f a%.5f,%.5f %.5f %d,%d %.5f,%.5f" (:x (:origin sperm)) (:y (:origin sperm)) 0 (- radius) radius radius 0 1 1 radius radius ))
        (attr {:stroke "black", :fill "none", :stroke-width 1})
        (.transform (gstring/format "t%d,%dr-45" (- radius) radius))
  )))
    
; create a filled ring region between two radii
(defn create-filled-ring [paper sperm colour from to]
  (let  [ra (/ (+ (:cbase (:scales sperm)) from) (:cscale (:scales sperm)))
        rb (/ (+ (:cbase (:scales sperm)) to) (:cscale (:scales sperm)))]
    (-> (.path paper (gstring/format "M%.5f,%.5f   m%.5f,%.5f          v%.5f         a%.5f,%.5f    %.5f     %d,%d    %.5f,%.5f   h%.5f   Z  "                            
                             (:x (:origin sperm)) (:y (:origin sperm))   0 (- ra)   (- (- rb ra))    rb rb      0      1 0     rb rb   (- (- rb ra) )))
        (attr {:stroke "none", :fill colour, :stroke-width 1})
        (.transform "r135")
  )))

; create a pie slice between two angles and two radiii with a specified initial rotation
(defn create-filled-pie-slice [paper sperm rada radb angle offset rotation]
  (let  [ang (deg-to-rad (- 90 angle ))
         ra (float rada)
         rb (float radb)
         hrb (* 0.5 rb)
         rbra (- rb ra)
         large-arc-flag (if (>= angle 180.0) 1 0)
         firstsnap [ (* rb (js/Math.cos ang)) (- (* rb (js/Math.sin ang))) ]
         secondsnap [ (* ra (js/Math.cos ang)) (- (* ra (js/Math.sin ang))) ]
         thirdsnap [ (* ra (js/Math.cos (deg-to-rad (+ 90.0)))) (- (* ra (js/Math.sin (deg-to-rad (+ 90.0))))) ]
         ]
      (->(.path paper (gstring/format "M%.5f,%.5f     m0,%.5f        v%.5f     a%.5f,%.5f    %.5f     %d,%d                %.5f,%.5f    l%.5f,%.5f    a%.5f,%.5f %.5f %d,%d %.5f,%.5f  z"                       
                             (:x (:origin sperm)) (:y (:origin sperm))   (- ra)   (- rbra)    rb rb      0     large-arc-flag 1  (get firstsnap 0) (+ (get firstsnap 1) rb) 
                             (- (get secondsnap 0) (get firstsnap 0))  (- (get secondsnap 1) (get firstsnap 1)  )
                             ra ra 0 large-arc-flag 0 (- (get thirdsnap 0) (get secondsnap 0))  (- (get thirdsnap 1) (get secondsnap 1) ))) ; second arc                       
        (.transform (gstring/format "R%.5f %.5f,%.5f T%.5f,%.5f" rotation (:x (:origin sperm)) (:y (:origin sperm)) (get offset 0) (get offset 1) ) )
        )))

; create a line between two circles given their radii and an angle (rad)
(defn create-path-between-circles [paper ra rb angrad]
  (.path paper (gstring/format "M%.3f,%.3f L%.3f,%.3f" (* ra (js/Math.cos angrad)) (* ra (js/Math.sin angrad)) (* rb (js/Math.cos angrad)) (* rb (js/Math.sin angrad)) )))

; create the interior uncertainty ring
(defn create-interior-coloured-arc [paper sperm]
  (-> (create-filled-ring paper sperm (sample-colourmap (:uncertainty colourmaps) (:headuncertainty (:params sperm))) 0 (:vsl (:params sperm)))))

; create the VCL
(defn create-vcl [paper sperm]
  (-> (create-ring paper sperm (:vcl (:params sperm))) (attr {:stroke "#666"})))

; create the VSL
(defn create-vsl [paper sperm]
  (-> (create-ring paper sperm (:vsl (:params sperm))) (attr {:stroke "#666"})))

; create the VAP
(defn create-vap [paper sperm]
  (-> (create-ring paper sperm (:vap (:params sperm))) (attr {:stroke "#000"})))

; create the orientation arrow (SLD)
(defn create-orientation-arrow [paper sperm]
  (let [radius (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm))) (:cscale (:scales sperm)))]
    (-> (.path paper (gstring/format "M%.5f,%.5f m%.5f,%.5f l%.5f,%.5f l%.5f,%.5f z " (:x (:origin sperm)) (:y (:origin sperm)) (- 10) (- radius) 10 (- 15) 10 15))
        (attr {:stroke "none", :fill "black" })
        )))
 
; create the mean angular displacement pie segment
(defn create-mad [paper sperm]
  (-> (create-filled-pie-slice paper sperm 0 (/ (:cbase (:scales sperm)) (:cscale (:scales sperm))) (:mad (:params sperm)) [0 0] (- (* 0.5 (:mad (:params sperm)))))
        (attr {:stroke "#666", :fill "white"})))

; create the grey filled tail (ftc, fta)
(defn create-arclength-tail [paper sperm]
  (let [zeroring (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))
        changeinangle (* (:ftc (:params sperm)) 1.0)
        flaglength (/ (* (:fta (:params sperm)) (:tscale (:scales sperm))) (:cscale (:scales sperm)))
        ]
    (-> (create-filled-pie-slice paper sperm 0 flaglength changeinangle [0 zeroring] (- (+ 180 (* 0.5 changeinangle))))
      (attr {:stroke "#666", :fill "#ccc"}))))
 
; create the tail asymmetry line with little spheres
(defn create-fas [paper sperm]
  (let [radius (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))
        k (+ (int (/ (:fta (:params sperm)) 50.0)) 1)
        dk (/ (* 50.0 (:tscale (:scales sperm))) (:cscale (:scales sperm)))
        asymm-length (* k dk)
        ang (* 30.0 (:fas (:params sperm)))
        r (* (* (/ (:cbase (:scales sperm)) (:cscale (:scales sperm))) 0.05) (:tscale (:scales sperm)))
        s (.set paper)
        width (/ (+ 1.0 (* 2.0 (:ftt (:params sperm)) (:tscale (:scales sperm)))) 1.0)
        spherecol (sample-colourmap (:uncertainty colourmaps) (:uf (:params sperm)))
        ]
    (-> s (.push
          ; line 
          (-> (.path paper (gstring/format "M0,%.5fv%.5f" 0 asymm-length))
              (attr {:stroke "#000", :stroke-width width}))
          )
      )
      (doseq [i (range (+ k 1))] (-> s (.push (-> (.ellipse paper 0 (* i dk) r r) (attr {:fill spherecol, :stroke-width 1 :stroke "#000"})))))
      (-> s (.transform (gstring/format "r%.3f %.3f %.3f T%.3f,%.3f" ang 0 0 (:x (:origin sperm)) (+ radius (:y (:origin sperm) )))))
  ))


; create beat cross frequency outer filled ring
(defn create-bcf-ring [paper sperm] 
  (let [r (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm))) (:cscale (:scales sperm)))
        r2 (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm)) 25.0) (:cscale (:scales sperm)))
        ang (* (:bcf (:params sperm)) 5.0)
        ]
    (-> (create-filled-pie-slice paper sperm r r2 ang [0 0] 225)
      (attr {:stroke "none", :fill "#ccc"}))))

; create the every x degrees guides around the outermost ring
(defn create-bcf-guides [paper sperm] 
  (let [ra (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm))) (:cscale (:scales sperm)))
        rb (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm)) 25.0) (:cscale (:scales sperm)))
        ang (* (:bcf (:params sperm)) 5.0)
        angr (deg-to-rad ang)
        g (+ 1 (* 30.0 (js/Math.ceil (/ ang 30.0))))
        ]
    (-> (.set paper)
        (push-to-set (map #(create-path-between-circles paper ra rb (deg-to-rad %1)) (range 135 (+ 135 g) 30)))
        (attr {:stroke "#999", :stroke-width 4})
        (.transform (gstring/format "t%.2f,%.2f" (:x (:origin sperm)) (:y (:origin sperm))))
    )))
    
(defn create-alh-lines [paper sperm] 
  (let [ra (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm))) (:cscale (:scales sperm)))
        rb (/ (+ (:cbase (:scales sperm)) (:vcl (:params sperm)) (:alh (:params sperm))) (:cscale (:scales sperm)))
        ang (* (:bcf (:params sperm)) 5.0)
        angr (deg-to-rad ang)
        g (+ 1 (* 30.0 (js/Math.ceil (/ ang 30.0))))
        ]
     (-> (.set paper)
        ; black guides - always-on
        (push-to-set (map #(create-path-between-circles paper ra rb (deg-to-rad %1)) [0 180]))
        (.transform (gstring/format "t%.2f,%.2f" (:x (:origin sperm)) (:y (:origin sperm))))
        (attr {:stroke "#000", :stroke-width 6}))
    ))
; create the zero-velocity ring, filled
(defn create-inner [paper sperm]
  (let [radius (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))]
    (-> (.circle paper (:x (:origin sperm)) (:y (:origin sperm)) radius radius)
        (attr {:stroke "#666", :stroke-width 1, :fill "#ccc"})
        )))

; create the crosshair guides for the zero-velocity ring
(defn create-inner-guides [paper sperm]
  (let [r (/ (:cbase (:scales sperm)) (:cscale (:scales sperm)))
        d (* r 2.0)
        ]
    (-> (.set paper)
        (.push (.path paper (gstring/format "M%.2f,0 h%.2f" (- r) d)))
        (.push (.path paper (gstring/format "M0 %.2f v%.2f" (- r) d)))
        (attr {:stroke "#fff", :stroke-width 3})
        (.transform (gstring/format "t%.2f,%.2f" (:x (:origin sperm)) (:y (:origin sperm))))
        )))

; create subtitle

; create a label from the name parameter
(defn create-label [paper sperm]
  (-> (.text paper (:x (:origin sperm)) (- (second (:size sperm)) 20) (:name (:params sperm)))
      (attr {:font-size 12, :font-weight "bold", :text-anchor "centre",  :fill "#EA553C"}))
  (->
      (.text paper (:x (:origin sperm)) (- (second (:size sperm)) 10) (or ((:params sperm) :subtitle) ""))
      (attr {:font-size 10,  :font-weight "normal", :text-anchor "centre",  :fill "#178487"})
    ))

; entrypoint for drawing the entire cell
(defn draw [sperm]
  (let [id (.attr (:div sperm) "id")
        w (first (:size sperm))
        h (second (:size sperm))
        paper (if (contains? @paper-stack (keyword id))
                (get-from-paper-stack id)
                (js/Raphael id w h))
        sperm (assoc sperm 
            :origin {:x (* 0.5 w) :y (* 0.5 h)}
            :scales (assoc globals :cscale (* 200.0 (/ 10.0 w)))
        )] 
    (add-to-paper-stack id paper)
    (set-defs-for-paper id (:params sperm))
    (.clear paper)
    (.setSize paper w h)
    (create-interior-coloured-arc paper sperm)
    (create-inner paper sperm)
    (create-inner-guides paper sperm)
    (create-mad paper sperm)
    (create-head paper sperm)
    (create-bcf-ring paper sperm)
    (create-alh-lines paper sperm)
    (create-bcf-guides paper sperm)
    (create-vcl paper sperm)
    (create-vsl paper sperm)
    (create-vap paper sperm)
    (create-arclength-tail paper sperm)
    (create-orientation-arrow paper sperm)
    (create-fas paper sperm)
    (create-label paper sperm)
    (clj->js (get-from-paper-stack id))
  ))
 
; given a url, grab a json file containing a key/val dictionary of sperm parameters and store into `presets` atom under id
(defn get-and-store-preset [url id callback]
  (let [keyid (keyword id)
    url-complete (str url id ".json")]
    (let-ajax [j {:url url-complete
                  :dataType :json}]
      (js/console.log (clj->js j))
      (swap! presets assoc keyid (js->clj j :keywordize-keys true))
      (if callback (callback id))
      )))

; get all known presets from server
(defn get-and-store-presets [resourceurl callback]
  (get-and-store-preset resourceurl "human" callback)
  (get-and-store-preset resourceurl "animal" callback))

; initialise ourselves for the first time
(defn init [resourceurl callback]
  (get-and-store-presets resourceurl callback))

; exposed functionality
(defn ^:export _getDefsForPaper [paper] (clj->js (get-defs-for-paper paper)))

(defn ^:export _draw [div, size]
  (let [sperm {:div div :size size :origin origin :scales globals :params currsperm}]
    (draw sperm)))

(defn ^:export _drawManual [div size props]
  (let [sperm {:div div :size size :origin origin :scales globals :params (walk/keywordize-keys (js->clj props :keywordize-keys true))}]
    (draw sperm)))

(defn ^:export _drawHumanPreset [div id size]
  (let [id (keyword (str id))
      params (merge defaultsperm (id (:human @presets)))
     sperm {:div div :size size :origin origin :scales globals :params params}]
    (draw sperm)
    (-> (clj->js params))
    ))

(defn ^:export _drawAnimalPreset [div id size]
 (let [id (keyword (str id))
      params (merge defaultsperm (id (:animal @presets)))
     sperm {:div div :size size :origin origin :scales globals :params params}]
    (draw sperm)
    (-> (clj->js params))
   ))

(defn ^:export _drawParams [div params size]
 (let [params (merge defaultsperm (js->clj params :keywordize-keys true))
       sperm {:div div :size size :origin origin :scales globals :params params}]
   (->(draw sperm))))

(defn ^:export _init [resourceurl callback] (init resourceurl callback))



