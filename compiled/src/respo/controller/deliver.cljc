
(ns respo.controller.deliver
  (:require [respo.controller.resolver :refer [find-event-target
                                               get-markup-at
                                               get-component-at]]
            [respo.util.detect :refer [component? element?]]))

(defn all-component-coords [markup]
  (if (component? markup)
    (cons (:coord markup) (all-component-coords (:tree markup)))
    (->>
      (:children markup)
      (map (fn [child-entry] (all-component-coords (val child-entry))))
      (apply concat))))

(defonce global-mutate-methods (atom {}))

(defn mutate-factory [global-element global-states]
  (fn [coord]
    (if (contains? @global-mutate-methods coord)
      (get @global-mutate-methods coord)
      (let [method (fn [& state-args]
                     (let [component (get-markup-at
                                       @global-element
                                       (subvec
                                         coord
                                         0
                                         (- (count coord) 1)))
                           init-state (:init-state component)
                           update-state (:update-state component)
                           state-path (conj coord 'data)
                           old-state (or
                                       (get-in
                                         @global-states
                                         state-path)
                                       (apply
                                         init-state
                                         (:args component)))
                           new-state (apply
                                       update-state
                                       (cons old-state state-args))]
                       (comment
                         println
                         "compare states:"
                         (pr-str @global-states)
                         state-path
                         (pr-str old-state)
                         (pr-str new-state))
                       (swap!
                         global-states
                         assoc-in
                         (conj coord 'data)
                         new-state)))]
        (swap! global-mutate-methods assoc coord method)
        method))))

(defn build-deliver-event [element-ref states-ref dispatch!]
  (fn [coord event-name simple-event]
    (let [target-element (find-event-target
                           @element-ref
                           coord
                           event-name)
          target-component (get-component-at @element-ref coord)
          target-listener (get (:event target-element) event-name)
          build-mutate (mutate-factory element-ref states-ref)
          new-coord (conj
                      (:coord target-component)
                      (:name target-component))
          mutate! (build-mutate new-coord)]
      (if (some? target-listener)
        (do
          (comment println "listener found:" coord event-name)
          (target-listener simple-event dispatch! mutate!))
        (comment println "found no listener:" coord event-name)))))
