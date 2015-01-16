(ns hypertext-ray.navigation
  (:use clj-webdriver.taxi hypertext-ray.finders hypertext-ray.re-maps)
  )

(defn start-driver [siteinfo]
  (let [url (:url siteinfo) ]
    (set-driver!  {:browser :chrome} url))
    siteinfo)

(defn get-siteinfo [sitehandle]
  (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts sitehandle))

;(set (map :class (classify-elements {:tag :a} anchor-re-map)))

(defn click-class [c]
  (let [found (first (filter-class c (classify-elements {:tag :a} anchor-re-map)))]
    (when found (-> found :xpath to-element click))))

(defn click-text [s]
  
  )

(defn search [text]
  (let [form (->> (classify-forms) (filter-class :search) first)]
    (when (not (nil? form))
      (let [searchfield (->> (classify-inputs form) (filter-class :search) first)]
        (quick-fill-submit {searchfield text})
        (Thread/sleep 2000)
        (submit searchfield)
        ) ) ))

