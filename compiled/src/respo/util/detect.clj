
(ns respo.util.detect
  (:import [respo.alias Component Element]))

(defn component? [x] (= Component (type x)))

(defn element? [x] (= Element (type x)))

(defn =vector [a b] false)
