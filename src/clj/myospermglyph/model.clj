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

; sample latest
(defn grab-latest [n]
  (json/write-str (map (fn [x] (dissoc x :created_at)) (sql/query db-name [(str "select * from submitted order by created_at limit " n)]))))

(defn migrated? []
  (-> (sql/query db-name 
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='submitted'")])
      first :count pos?))

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
