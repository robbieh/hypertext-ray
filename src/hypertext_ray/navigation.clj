(ns hypertext-ray.navigation
  (:use clj-webdriver.taxi hypertext-ray.finders hypertext-ray.re-maps)
  )

(defn start-driver [siteinfo]
  (let [url (:url siteinfo) ]
    (println "starting driver for url" url)
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

(defn click-anchor [re]
  (println "clicking anchor matching" re)
  (let [elements (find-elements {:tag :a})
        match (first (remove #(nil? (second %)) 
                             (for [e elements]
                               [e (or
                                    (re-matches re (or (attribute e :href) ""))
                                    (re-matches re (or (attribute e :class) ""))
                                    )])))
        ]
  (when match (click (first match)))

    )
  )

(defn search [text]
  (let [form (->> (classify-forms) (filter-class :search) first)]
    (when (not (nil? form))
      (let [searchfield (->> (classify-inputs form) (filter-class :search) first)]
        (quick-fill-submit {searchfield text})
        (Thread/sleep 2000)
        (submit searchfield)
        ) ) ))


(defn element-match? [e re-list]
  (some true?  (for [re re-list
                     at (map second (get-attributes-plus e))
                     ] 
                 (do
                   ((complement nil?) (re-matches re at))))) 
  
  )

(defn str-match? [s re-list]
  (some true? (for [re re-list] ((complement nil?) (re-matches re s))))) 

(defn match-elements [[q re-list]]
  (case q
    :title (str-match? (title) re-list)
    (some true? (map #(element-match? % re-list) (filter visible? (find-elements q))))
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

