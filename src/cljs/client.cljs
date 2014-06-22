(ns myospermglyph.server
    (:use [jayq.core :only [$]])
    (:require [jayq.core :as jq]))
 
(def $clickhere ($ :#clickhere))

(def globals {:cscale 3.00
              :cbase 100.0
              :hscale 4.00
              :tscale 1.50})
 
(def sperm {:name "Human"
          :vcl 57.0
          :vap 45.0
          :vsl 45
          :bcf 4.0
          :alh 10.3410815
          :mad 11.86
          :headlength 5.03
          :headwidth 3.21
          :arclength 54.5}
)

(def colours {:nouncertainty {:red 0.32941176, :green 0.32941176, :blue 0.84705882 }})
(defn raphaelcolour [colour]
  (.getRGB js/Raphael (format "rgb(%d,%d,%d)" 
      (int (* 255 (:red colour))) (int (* 255 (:green colour))) (int (* 255 (:blue colour))))
  )
) 


(jq/bind $clickhere :click (fn [evt] (js/alert (:vcl sperm))))

(defn- attr [object attributes]
  (.attr object (clj->js attributes)))

(defn create-head [paper sperm]
  (-> (.ellipse paper 200 200
              (/ (* (:headwidth sperm) (:hscale globals)) (:cscale globals))
              (/ (* (:headlength sperm) (:hscale globals)) (:cscale globals)))
      (attr {:stroke "black", :fill (raphaelcolour (:nouncertainty colours)), :stroke-width 1}))
  )

(defn create-arc [paper sperm radius]
    (-> (.path paper (format "M%d,%d m%d,%d a%d,%d %d %d,%d %d,%d" 200 200 0 (- radius) radius radius 0 1 1 radius radius ))
        (attr {:stroke "black", :fill "none", :stroke-width 1})
        (.transform (format "t%d,%dr-45" (- radius) radius))
  ))

(defn create-vcl [paper sperm]
  (let [rvsl (/ (+ (:cbase globals) (:vcl sperm)) (:cscale globals))]
    (-> (create-arc paper sperm rvsl))))

(defn create-vsl [paper sperm]
  (let [rvsl (/ (+ (:cbase globals) (:vsl sperm)) (:cscale globals))]
    (-> (create-arc paper sperm rvsl))))

(defn ^:export draw []
  (let [paper (js/Raphael 0 0 640 480)]
    (let [head (create-head paper sperm)]
    (let [vcl (create-vcl paper sperm)]
    (let [vsl (create-vsl paper sperm)]
  )))))
