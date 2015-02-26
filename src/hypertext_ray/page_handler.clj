(ns hypertext-ray.page-handler
  (:use [clj-webdriver.taxi]
     [hypertext-ray re-maps finders explore navigation login])
  
  )



(defn action-dispatch [siteinfo action current-page]
  (case action
    :login (do-login siteinfo)
    :wait (println "wait")
    :collect-data (find-data siteinfo current-page)
    :logout (println "logout")
    :quit (quit)
    siteinfo
    )
  )

(defn handle-page [siteinfo]
  (let [current-page (match-page siteinfo)
        action-list (current-page (:pageactions siteinfo))
        ]
    (println current-page action-list)
    (reduce #(action-dispatch % current-page) siteinfo action-list)
    
    )
  )

