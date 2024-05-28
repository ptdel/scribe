;; ---------------------------------------------------------
;; knowledge.scribe.-test
;;
;; Example unit tests for knowledge.scribe
;;
;; - `deftest` - test a specific function
;; - `testing` logically group assertions within a function test
;; - `is` assertion:  expected value then function call
;; ---------------------------------------------------------


(ns knowledge.scribe-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [knowledge.scribe :as scribe]))


(deftest application-test
  (testing "TODO: Start with a failing test, make it pass, then refactor"

    ;; TODO: fix greet function to pass test
    (is (= "knowledge application developed by the secret engineering team"
           (scribe/greet)))

    ;; TODO: fix test by calling greet with {:team-name "Practicalli Engineering"}
    (is (= (scribe/greet "Practicalli Engineering")
           "knowledge service developed by the Practicalli Engineering team"))))
