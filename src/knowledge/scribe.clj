(ns knowledge.scribe
  (:gen-class)
  (:require
   [aleph.http :as http]
   [knowledge.router :as router]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [com.brunobonacci.mulog :as mulog]))

(defonce mulog-publisher
  (mulog/start-publisher! {:type :console :pretty? true}))

(defmethod ig/init-key ::settings
  [_ {}]
  (ig/read-string (slurp "./config.edn")))

(defmethod ig/init-key ::database
  [_ {:keys [config]}]
  (let [db-config (:database config)]
    (jdbc/get-datasource db-config)))

(defmethod ig/init-key ::router
  [_ {:keys [db aws]}]
  (router/endpoints {:db db :aws aws}))

(defmethod ig/init-key ::server
  [_ {:keys [router]}]
  (http/start-server router {:port 8000}))

(defmethod ig/halt-key! ::server
  [_ server]
  (.close server))

(def system {::settings {}
             ::database {:config (ig/ref ::settings)}
             ::router   {:db  (ig/ref ::database)}
             ::server   {:router (ig/ref ::router)}})

(defn -main [& args]
  (mulog/set-global-context!
   {:app-name "scribe service" :version "0.1.0-SNAPSHOT"})
  (mulog/log ::application-startup :arguments args)
  (ig/init system))

(comment
  (mulog-publisher)
  (def running-system (ig/init system))
  (ig/halt! running-system))
