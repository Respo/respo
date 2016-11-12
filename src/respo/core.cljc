
(ns respo.core
  (:require [respo.alias :refer [create-comp div span]]
            [respo.render.expander :refer [render-app]]
            [respo.controller.deliver :refer [build-deliver-event mutate-factory]]
            [respo.render.differ :refer [find-element-diffs]]
            [respo.util.format :refer [purify-element mute-element]]
            [respo.controller.client
             :refer
             [initialize-instance activate-instance patch-instance]]
            [respo.polyfill :refer [log*]]
            [respo.util.gc :refer [find-removed apply-remove]]))

(defonce global-element (atom nil))

(defonce cache-element (atom nil))

(defn render-element [markup states-ref]
  (let [build-mutate (mutate-factory global-element states-ref)]
    (render-app markup @states-ref build-mutate @cache-element)))

(defn mount-app! [markup target dispatch! states-ref]
  (let [element (render-element markup states-ref)
        deliver-event (build-deliver-event global-element dispatch!)]
    (comment println "mount app")
    (initialize-instance target deliver-event)
    (activate-instance (purify-element element) target deliver-event)
    (reset! global-element element)
    (reset! cache-element element)))

(defn rerender-app! [markup target dispatch! states-ref]
  (let [element (render-element markup states-ref)
        deliver-event (build-deliver-event global-element dispatch!)
        changes (find-element-diffs [] [] @global-element element)]
    (comment println @global-element)
    (comment println "changes:" (pr-str (mapv (partial take 2) changes)))
    (patch-instance changes target deliver-event)
    (reset! global-element element)
    (reset! cache-element element)))

(defn render! [markup target dispatch states-ref]
  (if (some? @global-element)
    (rerender-app! markup target dispatch states-ref)
    (mount-app! markup target dispatch states-ref)))

(defn falsify-stage! [target element dispatch!]
  (reset! global-element (mute-element element))
  (reset! cache-element element)
  (let [deliver-event (build-deliver-event global-element dispatch!)]
    (initialize-instance target deliver-event)))

(defn clear-cache! [] (reset! cache-element nil))

(defn gc-states! [states-ref]
  (let [removed-paths (find-removed @states-ref @global-element [])
        new-states (apply-remove @states-ref removed-paths)]
    (comment println @states-ref)
    (comment println removed-paths)
    (reset! states-ref new-states)))