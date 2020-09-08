(ns hoquet.react
  (:require [clojure.string :as cstring]))

(def ^{:doc "Regular expression that parses a CSS-style id and class from a tag name." :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn as-str [x]
  (if (or (keyword? x) (symbol? x))
    (name x)
    (str x)))

(defn normalize-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (str tag " is not a valid tag name")))
  (let [[_ tag id class] (re-matches re-tag (as-str tag))
        tag-attrs        {:id id
                          :class (when class (cstring/replace class "." " "))}
        map-attrs        (first content)]
    (if (map? map-attrs)
      [tag (merge tag-attrs map-attrs) (next content)]
      [tag tag-attrs content])))

(def ^:dynamic
  ^{:doc "The function that creates an element.
          The function signature is: [component attributes & children]"}
  *create-element* list)

(defn create-element
  ([component] (create-element component nil))
  ([component attributes & children]
   (apply *create-element* component attributes children)))

(declare render-react)

(defn render-element
  "Render a tag vector as a HTML element."
  [element]
  (let [[tag attrs content] (normalize-element element)]
    (apply create-element tag attrs (map render-react content))))

(defn render-react
  "Turn a Clojure data type into a string of HTML.
   In Hiccup, sequences are expanded out into the body. This is particularly useful for macros like `for`."
  [x]
  (cond
    (vector? x) (render-element x)
    (seq? x) (apply str (map render-react x))
    :else (as-str x)))

(comment
  (binding [*create-element* (fn [component attributes & children]
                               (apply list 'create-element component attributes children))]
    (render-element [:div.foo
                     [:p.aa [:span 1]]
                     [:p.vv 2]])))
