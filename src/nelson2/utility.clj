(ns nelson2.utility
  (:gen-class)
  (:require [clojure.edn :as edn]))

(defn dec2base32 [N]
  ;;convert a decimal into base 32
  (if (= "_" (str (first (str N))))
    (apply str (map #(str "_" %) (map #(Integer/toString
                                         (Integer/parseInt %) 32)
                                      (clojure.string/split (subs N 1) #"_"))))
    (apply str (map #(str "_" %) (map #(Integer/toString
                                         (Integer/parseInt %) 32)
                                      (clojure.string/split (str N) #"_"))))
    )


  )
(defn base32todec [N]
  (if (= "_" (str (first (str N))))
    (Integer/parseInt (apply str (map #(Integer/parseInt % 32) (clojure.string/split (subs N 1) #"_"))))
    (Integer/parseInt (apply str (map #(Integer/parseInt % 32) (clojure.string/split N #"_"))))
    )
)
(defn save-neurons [cluster]
  "save the neurons to their files"

  (when-not (= cluster {}) (spit (str "neuron-data/" (name (ffirst cluster)) ".neuron" ) (first cluster))
                           (save-neurons (into {} (rest cluster)))))

(defn diophantine [x y]
  (+ (* 124 x) y)
  )
(defn compress-data [byte-array]
  (clojure.string/replace (apply str (map #(str "_" %) byte-array) ) #"__" "_")
  ;;(if (= (count byte-array) 1) byte-array  (compress-data (vec (flatten (vector (diophantine (get byte-array 0) (get byte-array 1)) (vec (rest (rest byte-array))))))))
  )
(defn de-compress [byte-array n]
  (apply str (clojure.string/split (apply str byte-array) #"_"))
  ;;(if (= n 0) byte-array (de-compress (vec (flatten [(quot (get byte-array 0) 124) (- (get byte-array 0) (* 124 (quot (get byte-array 0) 124))) (rest byte-array)])) (dec n)))
  )
(defn average [coll]
  "Finds the average of the collection"
  (if (empty? coll) -1 (/ (apply + coll) (count coll)))
  )
(defn concept-level? [neuron-id]
  "Calculate the concept level of the neuron"
  (if (= "_" (str (first (name neuron-id))))
    (count (clojure.string/split (subs (name neuron-id) 1) #"_"))
    (count (clojure.string/split (name neuron-id) #"_"))
    )

  ;;(/ (Math/log (base32todec (name neuron-id))) (Math/log 128))
  )