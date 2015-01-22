(ns hypertext-ray.login
  (:use clj-webdriver.taxi hypertext-ray.finders hypertext-ray.re-maps)
;  (:require [hypertext-ray.finders :refer [classify-forms classify-urls]])
  (:import org.openqa.selenium.StaleElementReferenceException)
  )

(def logatom (atom []))
(defn logit [msg] (swap! logatom conj msg ))




(defn generate-action-by-type [xp tagmatch]
  (let [input-type (attribute (to-element xp) :type)]
    (case input-type
      "text" tagmatch
      "password" tagmatch
      "submit" submit
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

(defn classify-visible-form-inputs [e]
  (map #(vector (classify-element % input-re-map )(xpath %)) 
                    (filter-visible (find-elements-under e {:tag :input})))
  )


(defn find-login-form [] 
  (try (if-let [form (first (filter-visible (map #(to-element (:xpath %)) (filter #(= (:class %) :login) (classify-forms)))))] 
    form)
    (catch Exception e (.printStackTrace e) nil)))
(defn find-login-link []   
  (try (let [login-a (filter #(= (:class %) :login ) (classify-elements {:tag :a} anchor-re-map))] 
        (if-let [a (first (filter visible? (map #(find-element {:xpath (:xpath %)}) login-a)))]
          (element a)))
    (catch Exception e (.printStackTrace e) nil)))
(defn find-login-frame []
  (try (if-let [frame (first (filter #(= (:class %) :login ) (classify-elements {:tag :iframe} form-re-map)))]
    (element frame))
    (catch Exception e (.printStackTrace e) nil))) 

(defn add-submit-action [qa-maps inputs]
  (if-not (first (filter #(= submit (second %)) qa-maps))
    (concat qa-maps 
            (vector 
              (if-let [pwfield (second (first (filter #(= :pass (first %)) inputs)))] 
                {(to-element pwfield) submit}
                {(first (mapcat keys qa-maps)) submit})))
  qa-maps))

(defn fill-and-submit [siteinfo]
  (logit "starting fill and submit")
  (loop [stop 2]
    (Thread/sleep 1000)
    (if (zero? stop) nil
      (do
        (if-let [form (find-login-form)]
          (let [inputs (classify-visible-form-inputs form)
                query-action-map (get-query-action-map siteinfo inputs)
                submit-map (add-submit-action query-action-map inputs)
                ]
            (try (apply quick-fill-submit submit-map)
              (catch org.openqa.selenium.StaleElementReferenceException e (.printStackTrace e) nil)) )
          (logit "no form")) 
        (recur (dec stop))))))

(defn do-login [siteinfo]
  (logit "do-login start")
  (loop [stop 3]
    (logit (str "login try:" stop))
    (if (zero? stop) nil
      (if-let [form (find-login-form)] (fill-and-submit siteinfo)
        (do
          (if-let [link (find-login-link)] (do (logit (str "Clicking:" link)) (click link))
            (if-let [frame (find-login-frame)] (do (logit (str "Focus on frame:" frame)) (switch-to-frame frame))))
          (Thread/sleep 1000)
          (recur (dec stop))))))
  siteinfo
  )

(comment

    (find-elements [{:tag :form}])
  (filter-visible (find-elements-under (find-login-form) {:tag :input}))
  (classify-form-inputs (find-login-form))
  (logit "Test")
  (classify-forms)
  (identity @logatom)
  (let [siteinfo (hypertext-ray.navigation/get-siteinfo :foo)]
    (if-let [form (find-login-form)]
      (let [inputs (classify-visible-form-inputs form)
;            query-action-map (get-query-action-map siteinfo inputs)
;            submit-map (add-submit-action query-action-map inputs)
            ]
        ;(apply quick-fill-submit submit-map)
        inputs
        )
      ))
  )
