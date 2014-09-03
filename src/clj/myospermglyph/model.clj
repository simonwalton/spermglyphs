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

(ns myospermglyph.model
  (:require
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.java.jdbc :as sql]
    )
  (:use [hiccup.core]
        [compojure.core])
  (:gen-class)
  )

(def db-name (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/spermglyphs"))

(defn gen-uuid [] (subs (str (java.util.UUID/randomUUID)) 0 16))

; persist a sperm definition and return its id
(defn create [obj]
  (:id (first (sql/insert! db-name :submitted {:json (json/write-str (assoc obj :id (gen-uuid))) }))))

; grab-from-persistant
(defn grab [id]
  (:json (first (sql/query db-name [(str "select * from submitted where id = " id)]))))

; sample latest submitted entries from db and return JSON
(defn grab-latest [n]
  (json/write-str (map (fn [x] (dissoc x :created_at)) (sql/query db-name [(str "select * from submitted order by created_at limit " n)]))))

; return true if there's already a db setup here
(defn migrated? []
  (-> (sql/query db-name 
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='submitted'")])
      first :count pos?))

; create the tables necessary to support submissions
(defn migrate []
  (when (not (migrated?))
    (print "Creating database structure...") (flush)
    (sql/db-do-commands db-name
                        (sql/create-table-ddl
                         :submitted
                         [:id :serial "PRIMARY KEY"]
                         [:json :varchar "NOT NULL"]
                         [:created_at :timestamp
                          "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))
    (println " done")))
