(ns knowledge.handlers
  (:require
   [knowledge.s3 :as s3]
   [knowledge.database :as db]))

(defn meeting-upload-handler [request]
  (let [file (-> request :parameters :multipart :file)]
    (s3/put-object "meeting-test" (:filename file) (:tempfile file))
    {:status 200
     :body   {:name (:filename file)
              :size (:size file)}}))

(defn meeting-download-handler [request]
  (let [params (-> request :parameters :query)
        bucket (:bucket params)
        key (:key params)]
    {:status 200
     :body (s3/get-object bucket key)}))

(defn phrase-search-handler [request]
  (let [database (:db request)
        params   (-> request :parameters :query)
        begin    (:begin params)
        end      (:end params)
        phrase   (:phrase params)]
            {:status 200
     :headers {"content-type" "application/edn"}
     :body (db/search-for-occurrences-between database begin end phrase)}))
