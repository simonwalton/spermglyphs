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

(ns myospermglyph.common
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

(defn view-layout [& content]
  (html
      [:head
           [:meta {:http-equiv "Content-type"
                        :content "text/html; charset=utf-8"}]
           [:title "Ovii | Glyph-Based Video Visualization for Semen Analysis"]
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
            [:a {:href "http://github.com/simonwalton/spermglyphs" :alt "View our code on Github!"}[:i {:class "fa fa-github-square github-top"}]]
          ] 
         ]]]))

