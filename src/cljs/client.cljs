(ns myospermglyph.server
    (:require [jayq.core :as jq])
    (:use [jayq.core :only [$]]
)) 
 
(def globals {:cscale 3.00
              :cbase 100.0
              :hscale 4.00
              :tscale 1.50}) 

(def sperm {:name "Human"
          :vcl 205.26
          :vap 128.54
          :vsl 77.4
          :bcf 30.96
          :alh 47.12
          :mad 20
          :headlength 8.27
          :headwidth 3.65
          :headuncertainty 0.4
          :arclength 125
          }
)
 
; raphael utilities

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
  (-> (.ellipse paper 200 200
              (/ (* (:headwidth sperm) (:hscale globals)) (:cscale globals))
              (/ (* (:headlength sperm) (:hscale globals)) (:cscale globals)))
      (attr {:stroke "black", :fill (raphaelcolour (:nouncertainty colours)), :stroke-width 1}))
  )

(defn create-ring [paper value]
  (let [radius (/ (+ (:cbase globals) value) (:cscale globals))]
    (-> (.path paper (format "M%d,%d m%d,%d a%d,%d %d %d,%d %d,%d" 200 200 0 (- radius) radius radius 0 1 1 radius radius ))
        (attr {:stroke "black", :fill "none", :stroke-width 1})
        (.transform (format "t%d,%dr-45" (- radius) radius))
  )))

(defn create-filled-ring [paper colour from to]
  (let  [ra (/ (+ (:cbase globals) from) (:cscale globals))
        rb (/ (+ (:cbase globals) to) (:cscale globals))]
    (-> (.path paper (format "M%d,%d   m%d,%d          v%d         a%d,%d    %d     %d,%d    %d,%d   h%d   Z           "                            
                             200 200   0 (- ra)   (- (- rb ra))    rb rb      0      1 0     rb rb   (- (- rb ra) )))
        (attr {:stroke "none", :fill colour, :stroke-width 1})
        (.transform "r135")
  )))

; semen-specific
(defn create-interior-coloured-arc [paper sperm]
  (-> (create-filled-ring paper (sample-colourmap (:uncertainty colourmaps) (:headuncertainty sperm)) 0 (:vsl sperm))))

(defn create-vcl [paper sperm]
  (-> (create-ring paper (:vcl sperm))))

(defn create-vsl [paper sperm]
  (-> (create-ring paper (:vsl sperm))))

(defn create-vap [paper sperm]
  (-> (create-ring paper (:vap sperm))))

(defn create-orientation-arrow [paper sperm]
  (let [radius (/ (+ (:cbase globals) (:vcl sperm)) (:cscale globals))]
    (-> (.path paper (format "M%d,%d m%d,%d l%d,%d l%d,%d z " 200 200 (- 10) (- radius) 10 (- 15) 10 15))
        (attr {:stroke "none", :fill "black" })
        )))
 
(defn create-mad [paper sperm]
  (let  [ang (deg-to-rad (- 90 (:mad sperm) ))
         ra 0
         rb (/ (:cbase globals) (:cscale globals))
         rbra (- rb ra)
         firstsnap [ (* rb (js/Math.cos ang)) (- (* rb (js/Math.sin ang)))  ]
         secondsnap [ (* ra (js/Math.cos ang)) (- (* ra (js/Math.sin ang))) ]
         ]
    (-> (.path paper (format "M%d,%d   m0,%d        v%d     a%d,%d    %d     %d,%d                %d,%d                        l%d,%d   Z"                       
                             200 200   (- ra)   (- rbra)    rb rb      0      0 1  (get firstsnap 0) (+ (get firstsnap 1) rb)  (get secondsnap 0) (+ (get secondsnap 1) ra )))
        (attr {:stroke "red", :fill "pink"})
       ; (.transform (format "t%d,%dr%dt%d,%d" (- hrb) hrb (- (* 0.0 (:mad sperm))) hrb (- hrb) ))
        )))

(defn create-inner [paper sperm]
  (let [radius (/ (:cbase globals) (:cscale globals))]
    (-> (.circle paper 200 200 radius radius)
        (attr {:stroke "#666", :stroke-width 1 :fill "#ccc"})
        )))

(defn ^:export draw []
  (let [paper (js/Raphael "spermdiv" 500 480)]
    (let [filled-ring (create-interior-coloured-arc paper sperm)]
    (let [inner (create-inner paper sperm)]
    (let [head (create-head paper sperm)]
    (let [vcl (create-vcl paper sperm)]
    (let [vsl (create-vsl paper sperm)]
    (let [vap (create-vap paper sperm)]
    (let [mad (create-mad paper sperm)]
    (let [arrow (create-orientation-arrow paper sperm)]
  ))))))))))
