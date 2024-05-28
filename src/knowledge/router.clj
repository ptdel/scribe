(ns knowledge.router
  (:require
   [reitit.ring.malli]
   [reitit.coercion.malli]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [muuntaja.core :as m]
   [com.brunobonacci.mulog :as mulog]
   [knowledge.handlers :as handlers]))

(defn wrap
  "used to inject dependencies into handlers"
  [handler dep]
  (fn [request] (handler (merge request dep))))

(defn endpoints [config]
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info {:title       "scribe"
                              :description "ask questions about your meetings"
                              :version     "0.1.0"}
                       :tags [{:name "meeting" :description "search for meetings by tag(s)"}]}
             :handler (swagger/create-swagger-handler)}}]
     ["/meeting"
      ["/search" {:name ::meeting-search
                  :get  {:summary    "search for meetings with tag(s)"
                         :parameters {:query {:tags [:sequential string?]}}
                         :handler    (fn [_] _)}}]
      ["/upload" {:name ::upload
                  :post {:summary    "upload a meeting"
                         :parameters {:multipart {:file reitit.ring.malli/temp-file-part}
                                      :body      {:tags [:sequential string?]}}
                         :responses  {200 {:body {:name :string :size :int}}}
                         :handler    handlers/meeting-upload-handler}}]
      ["/tag/:id" {:name  ::tag
                   :patch {:summary    "add tag(s) to an existing meeting"
                           :parameters {:body {:tags [:sequential string?]}}
                           :handler    (fn [_] _)}}]
      ["/untag/:id" {:name ::untag
                     :put  {:summary    "remove tag(s) from an existing meeting"
                            :parameters {:query {:tags [:sequential string?]}}
                            :handler    (fn [_] _)}}]
      ["/download/:id" {:name ::download
                        :get  {:summary "download a meeting"
                               :handler handlers/meeting-download-handler}}]
      ["/list" {:name ::list
                :get  {:summary "list paginated meetings"
                       :handler (fn [_] _)}}]
      
      ["/delete/meeting/:id" {:name   ::delete-meeting
                              :delete {:summary "delete a meeting"
                                       :handler (fn [_] _)}}]
      ["/delete/tag/:id" {:name ::delete-tag
                          :get  {:summary "remove tag(s) (global)"
                                 :handler (fn [_] _)}}]
      ["/phrase/search" {:name ::phrase-search
                         :get  {:summary    "search for a phrase between two times"
                                :parameters {:query {:begin  string?
                                                     :end    string?
                                                     :phrase string?}}
                                :middleware [[wrap config]]
                                :handler    handlers/phrase-search-handler}}]]]
    {:exception pretty/exception
     :data      {:coercion   reitit.coercion.malli/coercion
                 :muuntaja   m/instance
                 :middleware [swagger/swagger-feature
                              muuntaja/format-middleware
                              parameters/parameters-middleware
                              multipart/multipart-middleware
                              exception/exception-middleware
                              rrc/coerce-request-middleware]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/doc"})
    (ring/create-default-handler))))

(comment (endpoints {}))
