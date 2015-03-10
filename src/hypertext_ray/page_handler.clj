(ns hypertext-ray.page-handler
  (:use [clj-webdriver.taxi]
     [hypertext-ray re-maps finders explore navigation login])
  
  )



(defn action-dispatch [siteinfo action current-page]
  (println "action:" action "page:" current-page)
  (let [choice  (if (vector? action) (first action) action)]
    (case choice
      :login (do-login siteinfo)
      :wait (do (println "wait") (Thread/sleep (second action)) siteinfo)
      :collect-data (find-data siteinfo current-page)
      :logout (do (println "logout") siteinfo)
      :quit (stop-driver siteinfo)
      :go (do (to (second action)) siteinfo)
      :click-text (do (click-text (second action)) siteinfo)
      :click-anchor (do (click-anchor (second action)) siteinfo)
      ))
  )

(defn handle-page [siteinfo]
  (let [current-page (match-page siteinfo)
        action-list (current-page (:pageactions siteinfo))
        ]
    (reduce #(action-dispatch %1 %2 current-page) siteinfo action-list)
    
    )
  )


(defn handle-site [siteinfo]
  (let [siteatom (atom siteinfo)]
    (reset! siteatom (-> @siteatom start-driver))
    (loop [x 10]
      (when (and (> x 0) (not (true? (:done @siteatom))))
        (reset! siteatom (-> @siteatom handle-page))
        (recur (dec x))
        )
      )
    
  @siteatom
    )
  )
