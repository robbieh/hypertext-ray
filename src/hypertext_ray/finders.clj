(ns hypertext-ray.finders
  (:use clj-webdriver.taxi
     hypertext-ray.re-maps))


;TODO - maybe this should be a bug report for clj-webdriver? if the element doesn't have the requested tag,
;it throws this NullPointerException
(defn failsafe-attribute 
  "Attempts to read given attribute's value from HTML element.
  Returns nil instead of NullPointerException if this fails." 
  [e attr] (try (attribute e attr) (catch NullPointerException e nil)))

(defn failsafe-text [e] (failsafe-attribute e :text))


;TODO: should this be a clj-webdriver bug? it's annoying.
(defn to-element 
  "Return an element given an xpath or an element."
  [thing] (if (string? thing)  ;assume xpath.
            (find-element {:xpath thing}) 
            thing))

(defn get-attributes 
  "Returns a list of attributes for the given HTML element."
  [e] 
  (into {}  (for [a (execute-script (slurp (clojure.java.io/resource "get_attrs.js")) [(xpath e)])]
                                    [(keyword a) (attribute e (keyword a))])))

(defn get-attributes-plus 
  "Returns attributes of element along with (text e) and (value e)"
  [e] (merge (get-attributes e)
             (hash-map :text (text e) :value (text e)))
  )

(defn find-table-rows [e] (find-elements-under e {:tag :tr})) 
(defn find-header-cells [e] (find-elements-under e {:tag :th}))
(defn find-row-cells [e] (find-elements-under e {:tag :td}))

(defn extract-table-data 
  "Given a table element, returns a map with :headers and :cells"
  [e]
  (let [rows (find-table-rows e)
        headers (for [r rows] (mapv failsafe-text (find-header-cells r)))
        cells (for [r rows] (mapv failsafe-text (find-row-cells r)))]
    {:headers headers :cells cells}))


(defn filter-visible 
  "Removes invisible elements from a sequence"
  [s] (filter visible? (map to-element s)))

(defn filter-class
  "Returns only elements which match the class. Works with output from classify-elements."
  [c s]
  (filter #(= c (:class %)) s))

;NOTE: I don't like that I have to remove newlines. 
;I thought Java's regexes suppored multiline matches with (?m)
;but it doesn't work
(defn search-keys-and-vals 
  "Apply regex to both keys and vals of given map.
  Returns flat list of matches."
  [m re]
  (remove nil? (map #(re-matches re (clojure.string/replace % "\n" ""))
                    (concat (map name (keys m))(vals m)) )))

(defn classify-element
  "Attempt to give a classification to the given element
  by finding which regexes from the re-map return the most matches.
  Will return :unknown if no matches are found.
  Pass :return-map in flags to get the count by type instead of the final guessed type. "
  [e re-map & flags]
  (let [attrs   (get-attributes-plus e)
        results (apply merge (for [[tag re-list] re-map]
                         {tag (reduce + (for [re re-list] (count (search-keys-and-vals attrs re))))}
                         ))
        [best-match best-count]    (reduce #(if (> (val %1) (val %2)) %1 %2)  results)  
        best  (if (zero? best-count) :unknown best-match)   ]
    (if (some #(= :return-map %) flags) results best)))


(defn classify-elements 
  "Classify all elements which match the given query.
  Pass :drop-unknowns to filter out elements which could not be classified."
  [q re-map & flags]
  (let [matches (for [e (find-elements [q])] 
                  {:class (apply classify-element e re-map flags)
                   :xpath (xpath e)})]
    (if (some #(= :drop-unknowns %) flags) 
      (remove #(= :unknown (:class %)) matches)
      matches)))


(defn classify-anchors [& flags]
  (for [anchor (find-elements [{:tag :a} ])] 
    {:class (apply classify-element anchor anchor-re-map flags)
     :text (text anchor)
     :href (failsafe-attribute anchor :href)
     :xpath (xpath anchor) }))

(defn classify-inputs [form & flags]
  (for [input (find-elements-under form {:tag :input})] 
    {:class (apply classify-element input input-re-map flags)
     :text (text input)
     :value (value input)
     :xpath (xpath input) }))

(defn classify-form-inputs [form]
  (for [input (find-elements-under form {:tag :input})]
    (apply classify-element input form-re-map [:return-map])
    ))

(defn classify-forms "Finds all forms on current page and attempts to determine the type"
  [& flags]
  (for [form (find-elements [{:tag :form} ])] 
    (let [inputs   (classify-form-inputs form)
          form-map (apply classify-element form form-re-map [:return-map])
          combo    (concat (list form-map) inputs)
          summed   (apply (partial merge-with +) combo)
          [best-match best-count]    (reduce #(if (> (val %1) (val %2)) %1 %2)  summed)  
          best  (if (zero? best-count) :unknown best-match)    
          ]
      (if (some #(= :return-map %) flags)
        combo
        {:class best :xpath (xpath form)}))))
