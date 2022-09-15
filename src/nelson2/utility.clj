(ns nelson2.utility
  (:gen-class)
  (:require [clojure.edn :as edn]))
(defn dec2base32 [N]
    ;;convert a decimal into base 32
    (Integer/toString N 32))

(defn base32todec [N]
  (Integer/parseInt N 32)
  )
(defn save-neurons [cluster]
  "save the neurons to their files"

  (when-not (= cluster {}) (spit (str "neuron-data/" (name (ffirst cluster)) ".neuron" ) (first cluster))
                           (save-neurons (into {} (rest cluster)))))

(defn diophantine [x y]
  (+ (* 257 x) y)
  )
(defn compress-data [byte-array]
  (if (= (count byte-array) 1) byte-array  (compress-data (vec (flatten (vector (diophantine (get byte-array 0) (get byte-array 1)) (vec (rest (rest byte-array))))))))
  )
(defn de-compress [byte-array n]
  (if (= n 0) byte-array (de-compress (vec (flatten [(quot (get byte-array 0) 257) (- (get byte-array 0) (* 257 (quot (get byte-array 0) 257))) (rest byte-array)])) (dec n)))
  )
(defn concept-level? [neuron-id]
  "Calculate the concept level of the neuron"
  (/ (Math/log (base32todec (name neuron-id))) (Math/log 256))
  )