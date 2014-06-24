(ns myospermglyph.server
    (:require [jayq.core :as jq])
    (:use [jayq.core :only [$]]
)) 

(def origin {:x 280 :y 300})

(def globals {:cscale 3.00
              :cbase 350.0
              :hscale 4.00
              :tscale 1.50}) 

(def currsperm {:name "Human"
          :vcl 205.26
          :vap 128.54
          :vsl 77.4
          :bcf 30.96
          :alh 47.12
          :mad 45
          :headlength 8.27
          :headwidth 3.65
          :headuncertainty 0.4
          :fta 50.0
          :ftc 23.0
          :ftt 0.87
          :fas -0.1
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
  (-> (.ellipse paper (:x origin) (:y origin)
              (/ (* (:headwidth sperm) (:hscale globals)) (:cscale globals))
              (/ (* (:headlength sperm) (:hscale globals)) (:cscale globals)))
      (attr {:stroke "black", :fill (raphaelcolour (:nouncertainty colours)), :stroke-width 1}))
  )

(defn create-ring [paper value]
  (let [radius (/ (+ (:cbase globals) value) (:cscale globals))]
    (-> (.path paper (format "M%d,%d m%d,%d a%d,%d %d %d,%d %d,%d" (:x origin) (:y origin) 0 (- radius) radius radius 0 1 1 radius radius ))
        (attr {:stroke "black", :fill "none", :stroke-width 1})
        (.transform (format "t%d,%dr-45" (- radius) radius))
  )))

(defn create-filled-ring [paper colour from to]
  (let  [ra (/ (+ (:cbase globals) from) (:cscale globals))
        rb (/ (+ (:cbase globals) to) (:cscale globals))]
    (-> (.path paper (format "M%d,%d   m%d,%d          v%d         a%d,%d    %d     %d,%d    %d,%d   h%d   Z           "                            
                             (:x origin) (:y origin)   0 (- ra)   (- (- rb ra))    rb rb      0      1 0     rb rb   (- (- rb ra) )))
        (attr {:stroke "none", :fill colour, :stroke-width 1})
        (.transform "r135")
  )))

(defn create-filled-pie-slice [paper sperm rada radb angle offset rotation]
  (let  [ang (deg-to-rad (- 90 angle ))
         ra rada
         rb radb
         hrb (* 0.5 rb)
         rbra (- rb ra)
         firstsnap [ (* rb (js/Math.cos ang)) (- (* rb (js/Math.sin ang)))  ]
         secondsnap [ (* ra (js/Math.cos ang)) (- (* ra (js/Math.sin ang))) ]
         thirdsnap [ (* ra (js/Math.cos (deg-to-rad (+ 90)))) (- (* ra (js/Math.sin (deg-to-rad (+ 90))))) ]
         ]
    (-> (.path paper (format "M%d,%d   m0,%d        v%d     a%d,%d    %d     %d,%d                %d,%d                                          l%d,%d    a%d,%d %d %d,%d %d,%d  z"                       
                             (:x origin) (:y origin)   (- ra)   (- rbra)    rb rb      0      0 1  (get firstsnap 0) (+ (get firstsnap 1) rb) 
                             (- (get secondsnap 0) (get firstsnap 0))  (- (get secondsnap 1) (get firstsnap 1)  )
                             ra ra 0 0 0 (- (get thirdsnap 0) (get secondsnap 0))  (- (get thirdsnap 1) (get secondsnap 1) ))) ; second arc
                             
        (.transform (format "R%d %d,%d T%d,%d" rotation (:x origin) (:y origin) (get offset 0) (get offset 1) ) )
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
    (-> (.path paper (format "M%d,%d m%d,%d l%d,%d l%d,%d z " (:x origin) (:y origin) (- 10) (- radius) 10 (- 15) 10 15))
        (attr {:stroke "none", :fill "black" })
        )))
 
(defn create-mad [paper sperm]
  (-> (create-filled-pie-slice paper sperm 0 (/ (:cbase globals) (:cscale globals)) (:mad sperm) [0 0] (- (* 0.5 (:mad sperm))))
        (attr {:stroke "#666", :fill "white"})))
      
(defn create-arclength-tail [paper sperm]
  (let [zeroring (/ (:cbase globals) (:cscale globals))
        changeinangle (* (:ftc sperm) 16.0)
        flaglength (/ (* (:fta sperm) (:tscale globals)) (:cscale globals))
        ]
    (-> (create-filled-pie-slice paper sperm 0 (:fta sperm) flaglength [0 zeroring] (- (+ 180 (* 0.5 flaglength))))
      (attr {:stroke "#666", :fill "#ccc"}))))

(defn create-bcf-ring [paper sperm] 
  (let [r (/ (+ (:cbase globals) (:vcl sperm)) (:cscale globals))
        ang (:bcf sperm)
        ]
    (-> (create-filled-pie-slice paper sperm r (+ 20 r) ang [0 0] 270)
      (attr {:stroke "none", :fill "#ccc"}))))

(defn create-inner [paper sperm]
  (let [radius (/ (:cbase globals) (:cscale globals))]
    (-> (.circle paper (:x origin) (:y origin) radius radius)
        (attr {:stroke "#666", :stroke-width 1, :fill "#ccc"})
        )))

; entry

(def mainpaper (js/Raphael "spermdiv" 600 800))

(defn draw [sperm]
    (.clear mainpaper)
    (let [paper mainpaper]
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
  ))

(defn update []
  (draw (assoc currsperm
       :vcl (.getValue (js/getSlider "vcl"))
       :vap (.getValue (js/getSlider "vap"))
       :vsl (.getValue (js/getSlider "vsl"))
       :bcf (.getValue (js/getSlider "bcf"))
       )))

(defn ^:export _init [] ());def mainpaper (js/Raphael "spermdiv" 500 480)))
(defn ^:export _draw [] (draw currsperm))
(defn ^:export _update [] (update))

; jq


