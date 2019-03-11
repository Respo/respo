
(ns respo.controller.resolve
  (:require [clojure.string :as string]
            [respo.util.detect :refer [component? element?]]
            [respo.util.list :refer [filter-first]]))

(defn get-markup-at [markup coord]
  (comment println "markup:" (pr-str coord))
  (if (empty? coord)
    markup
    (if (component? markup)
      (recur (:tree markup) (rest coord))
      (let [coord-head (first coord)
            child-pair (filter-first
                        (fn [child-entry] (= (get child-entry 0) coord-head))
                        (:children markup))]
        (if (some? child-pair)
          (get-markup-at (get child-pair 1) (rest coord))
          (throw (js/Error. (str "child not found:" coord (map first (:children markup))))))))))

(defn find-event-target [element coord event-name]
  (let [target-element (get-markup-at element coord), element-exists? (some? target-element)]
    (comment println "target element:" (pr-str event-name))
    (if (and element-exists? (some? (get (:event target-element) event-name)))
      target-element
      (if (= coord [])
        nil
        (if element-exists?
          (recur element (subvec coord 0 (- (count coord) 1)) event-name)
          nil)))))

(defn get-component-at
  ([markup coord] (get-component-at nil markup coord))
  ([acc markup coord]
   (if (empty? coord)
     acc
     (let [coord-head (first coord)]
       (if (component? markup)
         (if (= (:name markup) coord-head) (recur markup (:tree markup) (rest coord)) nil)
         (let [child-pair (filter-first
                           (fn [child-entry] (= (get child-entry 0) coord-head))
                           (:children markup))]
           (if (some? child-pair) (recur acc (last child-pair) (rest coord)) nil)))))))

(defn build-deliver-event [*global-element dispatch!]
  (fn [coord event-name simple-event]
    (let [target-element (find-event-target @*global-element coord event-name)
          target-component (get-component-at @*global-element coord)
          this-cursor (:cursor target-component)
          target-listener (get (:event target-element) event-name)
          mutate! (fn
                    ([next-state] (dispatch! :states [this-cursor next-state]))
                    ([cursor next-state] (dispatch! :states [cursor next-state])))]
      (if (or (= :input (:name target-element)) (= :textarea (:name target-element)))
        (let [virtual-attrs (->> target-element :attrs (into {}))
              target-element (.. (:event simple-event) -target)]
          (if (not= (:value virtual-attrs) (.-value target-element))
            (set! (.-value target-element) (:value virtual-attrs)))
          (if (and (= :input (:name target-element))
                   (not= (:checked virtual-attrs) (.-checked target-element)))
            (set! (.-checked target-element) (:checked virtual-attrs)))))
      (if (some? target-listener)
        (do
         (comment println "listener found:" coord event-name)
         (target-listener simple-event dispatch! mutate!))
        (comment println "found no listener:" coord event-name)))))
