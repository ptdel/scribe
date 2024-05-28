(ns knowledge.transcripts
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [clojure.string :as string]))

(def cli-options {:filepath {:alias :p
                             :desc  "path to look for .srt files"}})

(def options (cli/parse-opts *command-line-args* {:spec cli-options}))

(defn transcript-split-entries [entries]
  (string/split entries #"\n\n"))

(defn ts->range [ts]
  (str "[" (first ts) ", " (second ts) ")"))

(defn transcript-parse-ts [ts]
  (-> ts
      (string/replace #"," ".")
      (string/split #"\s*-->\s*")
      (ts->range)))

(defn transcript-parse-entry [entry]
  (let [[_ t txt] (string/split entry #"\n")
        ts        (transcript-parse-ts t)]
    {:span ts :contents txt}))

(defn transcripts-list [filepath]
  {:pre [(fs/directory? filepath)]}
  (map str (fs/glob filepath "**{.srt}")))

(defn transcripts-read [file]
  {:pre [(fs/exists? file)]}
  (slurp file))

(defn transcripts-parse [transcripts]
  (mapv #(transcript-parse-entry %) (transcript-split-entries transcripts)))

(defn path->release [filepath]
  (-> filepath
      (fs/file-name)
      (fs/split-ext)
      (first)))

(defn transcripts-entry [filepath]
  (let [release (path->release filepath)
        transcripts (-> filepath (transcripts-read) (transcripts-parse))]
    {:release release :transcripts transcripts}))

(comment
  (def example-parsed-transcript-files
    (let [files (transcripts-list (:filepath options))]
      (map #(transcripts-entry %) files))))
