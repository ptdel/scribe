(ns knowledge.s3
  (:require
   [clojure.java.io :as io]
   [cognitect.aws.client.api :as aws]
   [cognitect.aws.credentials :as creds]))

;; uses `default` in `~/.aws/credentials` unless configured otherwise.
(def profile (creds/profile-credentials-provider "personal"))

(def s3 (aws/client {:api :s3
                     :region "us-east-1"
                     :credentials-provider profile}))

(defn list-objects [bucket]
  (aws/invoke s3 {:op :ListObjectsV2
                  :request {:Bucket bucket}}))

(defn get-object [bucket key]
  (aws/invoke s3 {:op :GetObject
                  :request {:Bucket bucket :Key key}}))

(defn put-object [bucket key file]
  (aws/invoke s3 {:op :PutObject
                  :request {:Bucket bucket :Key key
                            :Body (io/input-stream file)}}))
