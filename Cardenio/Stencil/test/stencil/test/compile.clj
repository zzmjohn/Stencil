(ns Stencil.test.compile
  (:require [stencil.compile :as c])
  (:use [clojure.test]))

(deftest default-for-type 
  (is (= (c/default-for-type 'int) 0))
  (is (= (c/default-for-type 'double) 0))
  (is (= (c/default-for-type 'float) 0))
  (is (= (c/default-for-type 'long) 0))
  (is (= (c/default-for-type 'string) "")))

(deftest meta->map
  (is (= (c/meta->map '($meta)) {}))
  (is (= (c/meta->map '($meta (a b) (c d))) {'a 'b, 'c 'd})))

(deftest map->meta
  (is (= (c/map->meta {'a 'b, 'c 'd}) '($meta (a b) (c d))))
  (is (= (c/map->meta {}) '($meta))))

(deftest meta-vals
  (is (= (c/meta-vals '($meta (a b) (c d) (e f))) '(b d f)))
  (is (= (c/meta-vals '($meta)) '())))

(deftest meta-keys 
  (is (= (c/meta-keys '($meta (a b) (c d) (e f))) #{'a 'c 'e}))
  (is (= (c/meta-keys '($meta)) #{})))
