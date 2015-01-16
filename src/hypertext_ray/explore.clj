(ns hypertext-ray.explore
  (:use clj-webdriver.taxi
     hypertext-ray.finders
     hypertext-ray.re-maps))




(defn form-info [e] {
            :attributes  (get-attributes e)
            :tag (tag e)
            :text (text e)
            :value (value e)
            :visible (visible? e)
            :xpath (xpath e) })


(defn forms-info []
  (for [e (find-elements [{:tag :form}])]
    (form-info e)))

(defn explore-forms 
  "returns a vector of forms and related info"
  []
  (for [form (find-elements [{:tag :form} ])]
    ;(for [attrs (get-attributes form)] attrs)
      (for [input  (find-elements-under form {:tag :input})]
        (get-attributes input))) )



(defn explore-tables
  ""
  []
  (for [table (find-elements [{ :tag :table}])]
    (for [header (find-elements-under table {:tag :th})]
      (get-attributes header)
      )
    )
  )
