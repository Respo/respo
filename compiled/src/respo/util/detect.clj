
(ns respo.util.detect)

(defn component? [x] (contains? x :tree))

(defn element? [x] (contains? x :event))

(defn =vector [a b] false)
