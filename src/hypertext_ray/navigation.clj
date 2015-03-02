(ns hypertext-ray.navigation
  (:use clj-webdriver.taxi hypertext-ray.finders hypertext-ray.re-maps)
  )

(defn start-driver [siteinfo]
  (let [url (:url siteinfo) ]
    (set-driver!  {:browser :chrome} url))
    siteinfo)

(defn stop-driver [siteinfo]
  (println "stopping driver")
  (quit)
  (assoc siteinfo :done true)
  )

;(defn get-siteinfo [sitehandle] (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts sitehandle))
;(defn get-siteconfig [id] (-> (System/getProperty "user.home") (str "/.webscrape-config/accounts-" (name id) ".rc") slurp read-string ))

;(set (map :class (classify-elements {:tag :a} anchor-re-map)))

(defn click-class [c]
  (let [found (first (filter-class c (classify-elements {:tag :a} anchor-re-map)))]
    (when found (-> found :xpath to-element click))))

(defn click-text [re]
  (let [found (first (filter-class :match (classify-elements {:tag :a} {:match [re]})))]
    (when found (-> found :xpath to-element click))))

(defn search [text]
  (let [form (->> (classify-forms) (filter-class :search) first)]
    (when (not (nil? form))
      (let [searchfield (->> (classify-inputs form) (filter-class :search) first)]
        (quick-fill-submit {searchfield text})
        (Thread/sleep 2000)
        (submit searchfield)
        ) ) ))


(defn element-match? [e re-list]
  (some true?  (for [re re-list] ((complement nil?) (re-matches re (text e))))) )

(defn str-match? [s re-list]
  (some true? (for [re re-list] ((complement nil?) (re-matches re s))))) 

(defn match-elements [[q re-list]]
  (case q
    :title (str-match? (title) re-list)
    (pmap #(vector (element-match? % re-list)) (find-elements q))
    )
  )

(defn match-page'
  [siteinfo]
  (for [[k v] (:pagematchers siteinfo)]
    [k (first (map match-elements (partition 2 v)))]
    )
  )

(defn match-page 
  ""
  [siteinfo]
  (let [results (match-page' siteinfo)]
    (first (first (filter #(true? (last %)) results))))
  
  )

(comment
  (as-> (get-siteconfig "gng") s 
      (match-page (:pagematchers s))
      )
  
  )

