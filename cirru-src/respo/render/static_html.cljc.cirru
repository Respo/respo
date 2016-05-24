
ns respo.render.static-html $ :require
  [] clojure.string :as string
  [] respo.util.format :refer $ [] prop->attr
  [] respo.util.detect :refer $ [] component? element?

defn style->string (styles)
  string/join | $ ->> styles $ map $ fn (entry)
    let
      (k $ first entry)
        v $ last entry
      str (name k)
        , |: v |;

defn entry->string (entry)
  let
    (k $ first entry)
      v $ last entry
    str
      prop->attr $ name k
      , |=
      pr-str $ if (= k :style)
        style->string v
        , v

defn props->string (props)
  ->> props
    filter $ fn (entry)
      let
        (k $ first entry)
        not $ re-matches (re-pattern |^:on-.+)
          str k

    map entry->string
    string/join "| "

defn element->string (element)
  let
    (tag-name $ name $ :name element)
      props $ :props element
      text-inside $ or (:innerHTML props)
        :inner-text props
      formatted-coord $ pr-str $ :coord element
      formatted-event $ pr-str $ into ([]) $ keys (:event element)
      tailored-props $ -> (:attrs element)
        dissoc :innerHTML
        dissoc :inner-text
        merge $ {}
          :data-coord formatted-coord
          :data-event formatted-event
      props-in-string $ props->string tailored-props
      children $ ->> (:children element)
        map $ fn (entry)
          let
              child $ last entry
            element->string child

    str |< tag-name
      if (> (count props-in-string) 0) "| " "|"
      , props-in-string |>
      or text-inside $ string/join | children
      , |</ tag-name |>

defn element->html (element)
  let
    (tag-name $ name $ :name element)
      props $ :props element
      text-inside $ or (:innerHTML props)
        :inner-text props
      tailored-props $ -> (:attrs element)
        dissoc :innerHTML
        dissoc :inner-text
      props-in-string $ props->string tailored-props
      children $ ->> (:children element)
        map $ fn (entry)
          let
              child $ last entry
            element->html child

    str |< tag-name
      if (> (count props-in-string) 0) "| " "|"
      , props-in-string |>
      or text-inside $ string/join | children
      , |</ tag-name |>
