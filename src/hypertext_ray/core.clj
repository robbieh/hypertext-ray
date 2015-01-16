(ns hypertext-ray.core
  (:use [clj-webdriver.taxi] [hypertext-ray.login :only [do-login]])
  )

(defn start-driver [siteinfo]
  (let [url (:url siteinfo) ]
    (set-driver!  {:browser :chrome} url))
    siteinfo)
(comment 
  
  

  ;thinking out loud...
  ;ideally
  (let [siteinfo (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :github)]
    (-> siteinfo
        start-driver
        do-login
        ;    (page-handler)
        ;    (write-data)
        ))
  (let [siteinfo (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :github)]
    (-> siteinfo do-login ))

  (let [siteinfo (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :ing)]
    (-> siteinfo start-driver do-login ))
  (let [siteinfo (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :ing)]
    (-> siteinfo start-driver do-login do-login))
  (quit)

)


