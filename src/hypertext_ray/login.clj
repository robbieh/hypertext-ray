(ns hypertext-ray.login
  (:use clj-webdriver.taxi hypertext-ray.finders hypertext-ray.re-maps)
;  (:require [hypertext-ray.finders :refer [classify-forms classify-urls]])
  )

(defn click-login []
  (let [login-a (filter #(= (:class %) :login ) (classify-elements {:tag :a} anchor-re-map)) 
        visible-a (filter visible? (map #(find-element {:xpath (:xpath %)}) login-a)) ]
    (try (click (first visible-a)) (catch Exception e :exception)) ))

(defn switch-to-login-frame []
  (let [frame (first (filter #(= (:class %) :login) (classify-elements {:tag :iframe} form-re-map)))]
    (try (switch-to-frame frame) (catch Exception e :exception))) )

(defn get-login-form 
  "Try to find a login form. If one isn't found, 
  try to find a 'login' type link and follow it 
  and search for the form again.
  Returns xpath of form or else nil"
  [] 
  (let [result 
        (loop [stop 3]
          (if (zero? stop) nil
          (let [form (first (filter #(= (:class %) :login) (classify-forms)))]
            (if-not (empty? form) 
              form
              (let [click-result (click-login)]
                (if #(= :exception click-result) (switch-to-login-frame)) 
                (Thread/sleep 2000)
                (recur (dec stop))))))
          )]
    (when result (:xpath result))
    )
)

(defn generate-action-by-type [xp tagmatch]
  (let [input-type (attribute (to-element xp) :type)]
    (case input-type
      "text" tagmatch
      "password" tagmatch
      "submit" nil
      "checkbox" (case tagmatch
                       :true #(if-not (selected? %) (click %))
                       :false #(if (selected? %) (click %))
                       nil) 
      ))
  )

(defn get-query-action-map [siteinfo inputs]
  (let [query-action-map (remove #(apply nil? (vals %)) (remove empty? 
                                 (for [[tag xp] inputs]
                                   (let [tagmatch (tag siteinfo)]
                                     (if tagmatch {(element {:xpath xp}) (generate-action-by-type xp tagmatch)} [])
                                     )
                                   )))]
    query-action-map))

(defn classify-visible-form-inputs [xp]
  (map #(vector (classify-element % input-re-map )(xpath %)) 
                    (filter-visible (find-elements-under (find-element {:xpath xp}) {:tag :input})))
  )

(defn do-login [siteinfo]
  (let [
        loginform (get-login-form)
        inputs    (classify-visible-form-inputs loginform)
        query-action-map (get-query-action-map siteinfo inputs)]
       
        (apply quick-fill query-action-map)  
        (submit (first (keys (first query-action-map))))
  ) 
  siteinfo
  )

(comment
  (quick-fill-submit  
         (submit (first (keys (first (get-query-action-map
   (hypertext-ray.navigation/get-siteinfo :foo)
   (classify-visible-form-inputs (get-login-form))))))))
  (map #(attribute % :type) (find-elements-under (element {:xpath (get-login-form)}) {:tag :input}))
  (map #(attribute % :type) (filter-visible (find-elements-under (element {:xpath (get-login-form)}) {:tag :input})))
  (map #(classify-element % input-re-map :return-map) (filter-visible (find-elements-under (element {:xpath (get-login-form)}) {:tag :input})))

  (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :ing)
  (do-login (-> (System/getProperty "user.home") (str "/.webscrape") slurp read-string :accounts :github))
 (remove #(= :unknown (:class %)) (classify-anchors)) 
  (classify-inputs (to-element (get-login-form)) :return-map) 
  (classify-element (to-element "/html/body/div/div/form/div[3]/input") input-re-map )
  (get-attributes-plus (to-element "/html/body/div/div/form/div[3]/input") )
  )

