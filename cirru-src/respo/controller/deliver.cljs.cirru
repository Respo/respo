
ns respo.controller.deliver $ :require $ respo.controller.resolver :refer $ [] find-event-target

defn do-states-gc (states-ref new-states)
  println "|states GC:" $ pr-str new-states
  reset! states-ref new-states

defonce id-counter $ atom 10

defn build-intent (store-ref updater)
  fn (intent-name intent-data)
    println |intent: intent-name $ pr-str intent-data
    reset! id-counter $ inc @id-counter
    let
      (op-id @id-counter)
        new-store $ updater @store-ref intent-name intent-data op-id
      println "|new store:" $ pr-str new-store
      reset! store-ref new-store

defn build-set-state (states-ref coord)
  fn (state-updates)
    println "|update state:" (pr-str coord)
      pr-str state-updates
    swap! states-ref assoc coord state-updates

defn build-deliver-event
  element-ref store-ref states-ref updater rerender-handler
  fn (coord event-name simple-event)
    let
      (target-element $ find-event-target @element-ref coord event-name)
        target-listener $ get (:events target-element)
          , event-name

      if (some? target-listener)
        do
          println "|listener found:" coord event-name
          target-listener simple-event (build-intent store-ref updater)
            build-set-state states-ref $ :component-coord target-element
          rerender-handler

        println "|found no listener:" coord event-name