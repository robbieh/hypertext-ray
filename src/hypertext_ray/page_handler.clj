(ns hypertext-ray.page-handler
  (:use [clj-webdriver.taxi]
     [hypertext-ray re-maps finders explore navigation login])
  
  )



(defn action-dispatch [siteinfo action current-page]
  (println "action:" action "page:" current-page)
  (case action
    :login (do-login siteinfo)
    :wait (do (println "wait") siteinfo)
    :collect-data (find-data siteinfo current-page)
    :logout (do (println "logout") siteinfo)
    :quit (do (println "quit") siteinfo)
    )
  )

(defn handle-page [siteinfo]
  (let [current-page (match-page siteinfo)
        action-list (current-page (:pageactions siteinfo))
        ]
    (reduce #(action-dispatch %1 %2 current-page) siteinfo action-list)
    
    )
  )

