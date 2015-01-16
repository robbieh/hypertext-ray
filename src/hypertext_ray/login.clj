(ns hypertext-ray.login
  (:use clj-webdriver.taxi hypertext-ray.finders hypertext-ray.re-maps)
;  (:require [hypertext-ray.finders :refer [classify-forms classify-urls]])
  )

(defn click-login []
  (let [login-a (filter #(= (:class %) :login ) (classify-elements {:tag :a} anchor-re-map)) 
        visible-a (filter visible? (map #(find-element {:xpath (:xpath %)}) login-a))
        ]
    (try (click (first visible-a)) (catch Exception e :exception)) ))

(defn get-login-form [] 
  (let [result 
        (loop [stop 5]
          (if (zero? stop) :failed
          (let [form (first (filter #(= (:class %) :login) (classify-elements {:tag :form} form-re-map)))]
            (if (not (empty? form)) 
              form
              (do (click-login)
                  (Thread/sleep 2000)
                  (recur (dec stop))))))
          )]
    (:xpath result)
    )
)

(defn get-query-action-map [siteinfo inputs]
  (let [query-action-map (into {}  (remove empty? (for [[tag xp] inputs]
                                                    (let [tagmatch (tag siteinfo)]
                                                      (if tagmatch [{:xpath xp} tagmatch] [])
                                                      )
                                                    )))]
    query-action-map))


(defn do-login [siteinfo]
  (let [
        loginform (get-login-form)
        inputs    (map #(vector (classify-element % input-re-map )(xpath %)) 
                    (filter-visible (find-elements-under (find-element {:xpath loginform}) {:tag :input})))
        query-action-map (get-query-action-map siteinfo inputs)]
       
        (quick-fill-submit query-action-map)  
        (submit (key (first query-action-map)))
  )

  siteinfo
  )

(comment
  (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :ing)
  (do-login (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :ing))
 (remove #(= :unknown (:class %)) (classify-anchors)) 

  )

