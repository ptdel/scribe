(ns knowledge.database
  ;; HoneySQL has its own definitions for these keywords so we exclude them from the namespace
  (:refer-clojure :exclude [filter for group-by into partition-by set update])
  (:require
   [clojure.string :as s]
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [honey.sql.pg-ops :refer [atat]]))

;; FTS is a postgres-specific feature so we extend honeysql. Only what we need.
(sql/register-fn!
 :to-tsquery
 (fn [_ [phrase]]
   ["to_tsquery('english', ?)"
    (apply str (interpose " & " (s/split phrase #" ")))]))

(defn read-meetings [begin end phrase]
  (-> (h/select :meeting_id :span :contents)
      (h/from :transcript)
      (h/join [:meeting :e] [:= :transcript.meeting_id :e.id])
      (h/where [:between :release begin end]
               [atat :ts [:to-tsquery phrase]])
      (sql/format)))

(defn write-meeting [release]
  (-> (h/insert-into :meeting)
      (h/values [{:release release}])
      (sql/format)))

(defn write-transcripts [release transcripts]
  (let [meeting-id (-> (h/select :id)
                       (h/from :meeting)
                       (h/where [:= :release release]))
        rows (mapv #(assoc % :meeting_id meeting-id) transcripts)]
    (-> (h/insert-into :transcript)
        (h/values rows)
        (sql/format))))

(defn add-meeting-with-transcripts [data-source data]
  (let [release (:release data)
        transcripts (:transcripts data)]
    (jdbc/with-transaction [tx data-source]
      (jdbc/execute! tx (write-meeting release))
      (jdbc/execute! tx (write-transcripts release transcripts)))))

(defn search-for-occurrences-between [data-source begin end phrase]
  (jdbc/execute! data-source (read-meetings begin end phrase)))

(comment add-meeting-with-transcripts
         search-for-occurrences-between)
