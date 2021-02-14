
(ns respo.comp.inspect (:require [respo.core :refer [defcomp pre <>]]))

(defn grab-info [data]
  (cond
    (map? data) (str "Map/" (count data))
    (vector? data) (str "Vector/" (count data))
    (set? data) (str "Set/" (count data))
    (nil? data) "nil"
    (number? data) (str data)
    (keyword? data) (str data)
    (boolean? data) (str data)
    (fn? data) "Fn"
    :else (pr-str data)))

(defn on-click [data]
  (fn [e dispatch!]
    (let [raw (pr-str data)]
      (if (> (count raw) 60) (.log js/console (clj->js data)) (.log js/console raw)))))

(def style-data
  {:position :absolute,
   :background-color "hsl(240,100%,0%)",
   :color :white,
   :opacity 0.2,
   :font-size "12px",
   :font-family "Avenir,Verdana",
   :line-height "1.4em",
   :padding "2px 6px",
   :border-radius "4px",
   :max-width 160,
   :max-height 32,
   :white-space :normal,
   :overflow :ellipsis,
   :cursor :default})

(defcomp
 comp-inspect
 (tip data style)
 (pre
  {:inner-text (str tip ": " (grab-info data)),
   :style (merge style-data style),
   :on-click (on-click data)}))
