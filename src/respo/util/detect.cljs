
(ns respo.util.detect )

(defn =seq [xs ys]
  (let [a-empty? (empty? xs), b-empty? (empty? ys)]
    (if (and a-empty? b-empty?)
      true
      (if (or a-empty? b-empty?)
        false
        (let [x0 (first xs), y0 (first ys)]
          (if (= (type x0) (type y0))
            (if (or (fn? x0) (= x0 y0)) (recur (rest xs) (rest ys)) false)
            false))))))

(defn compare-values [x y]
  (if (= (type x) (type y)) (compare x y) (compare (str (type x)) (str (type y)))))

(defn component? [x] (and (map? x) (contains? x :tree) (contains? x :render)))

(defn element? [x] (and (map? x) (contains? x :attrs) (contains? x :style)))
