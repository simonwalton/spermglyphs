(ns myospermglyph.server
    (:use [jayq.core :only [$]])
    (:require [jayq.core :as jq]))
 
(def $clickhere ($ :#clickhere))

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
          :mad 36
          :headlength 8.27
          :headwidth 3.65
          :arclength 125}
)

(def colours {:nouncertainty {:red 0.32941176, :green 0.32941176, :blue 0.84705882 }})
(defn raphaelcolour [colour]
  (.getRGB js/Raphael (format "rgb(%d,%d,%d)" 
      (int (* 255 (:red colour))) (int (* 255 (:green colour))) (int (* 255 (:blue colour))))
  )
) 


;(jq/bind $clickhere :click (fn [evt] (js/alert (:vcl sperm))))

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

(defn create-vcl [paper sperm]
  (-> (create-ring paper (:vcl sperm))))

(defn create-vsl [paper sperm]
  (-> (create-ring paper (:vsl sperm))))

(defn create-vap [paper sperm]
  (-> (create-ring paper (:vap sperm))))

(defn create-inner [paper sperm]
  (let [radius (/ (:cbase globals) (:cscale globals))]
    (-> (.circle paper 200 200 radius radius)
        (attr {:stroke "#666", :stroke-width 1 :fill "#ccc"})
        )))

(defn ^:export draw []
  (let [paper (js/Raphael "spermdiv" 500 480)]
    (let [inner (create-inner paper sperm)]
    (let [head (create-head paper sperm)]
    (let [vcl (create-vcl paper sperm)]
    (let [vsl (create-vsl paper sperm)]
    (let [vap (create-vap paper sperm)]
  )))))))
