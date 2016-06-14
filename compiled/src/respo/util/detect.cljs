
(ns respo.util.detect
  (:require [respo.alias :refer [Component Element]]))

(defn component? [x] (= Component (type x)))

(defn element? [x] (= Element (type x)))

(defn =vector [a b]
  (if (not= (count a) (count b))
    false
    (loop [a1 a b1 b]
      (if (identical? (count a) 0)
        (if (identical? a1 b1)
          (recur (subvec a 1) (subvec b 1))
          true)))))
