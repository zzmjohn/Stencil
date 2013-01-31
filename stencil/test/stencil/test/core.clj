(ns stencil.test.core
  (:require [stencil.core :as c])
  (:require [stencil.transform :as t])
  (:require [clojure.walk])
  (:use [clojure.test]))

(deftest stencil-parse
  (is (= (c/parse "(a)") '(a)))
  (is (= (c/parse "(stencil Test)") '(stencil Test)))
  (is (= (c/parse "#(a,b,c)") '($tuple a b c)))
  (is (= (c/parse "#(a b c)") '($tuple a b c)))
  (is (= (c/parse "##(a b c)") '($ptuple a b c)))
  (is (= (c/parse "##())") '($ptuple)))
  (is (= (c/parse "(a {int})") '(a ($meta int))))
  (is (= (c/parse "[a]") '(tuple-ref a)))
  (is (= (c/parse "(a : b)") '(a $C b)))
  (is (= (c/parse "; some") '(comment " some")))
  (is (= (c/parse ";* some then more*;")) '(comment " some then more"))
  (is (= (c/parse "(;* some*; then more)")) '((comment " some") then more)
  ))

(def root "../tests/data/")
(deftest stencil-read
  (is (list? (c/read (str root "geometry/simpleLines.stencil"))))
  (is (list? (c/read (str root "geometry/simpleLines.tstencil")))))



(def root "../tests/data/")

(defn clean [p] (t/drop-comments p))

(defn same-structure? [p1 p2]
  (cond
    (and (not (seq? p1)) (not (seq? p2))) true
    (or (not (seq? p1)) (not (seq? p2))) false
    (and (empty? p1) (empty? p2)) true
    (or (empty? p1) (empty? p2)) false
    :else
      (and (= (count p1) (count p2))
           (every? true? (map same-structure? p1 p2)))))

(deftest compile-stencil-structure
  (is (same-structure?
        (clean (c/compile (str root "geometry/simplelines.stencil")))
        (clean (c/compile (str root "geometry/simplelines.tstencil"))))))

(deftest compile-stencil
  (is (= (clean (c/compile (str root "geometry/simplelines.stencil")))
         (clean (c/compile (str root "geometry/simplelines.tstencil"))))))

