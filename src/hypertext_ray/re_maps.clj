(ns hypertext-ray.re-maps )


(def anchor-re-map (read-string (slurp (clojure.java.io/resource "anchor-re.clj"))))
(def form-re-map  (read-string (slurp (clojure.java.io/resource "form-re.clj"))))
(def input-re-map (read-string (slurp (clojure.java.io/resource "input-re.clj")))) 
(def table-re-map (read-string (slurp (clojure.java.io/resource "table-re.clj"))))

