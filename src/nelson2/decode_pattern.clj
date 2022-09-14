(ns nelson_clojure.decode-pattern
  (:gen-class)
  (:require [nelson_clojure.utility :as utility]))

(defn decode-bytearr [byte-arr]
"convert the byte array to string"
  (String. byte-arr)
  )
(defn pattern2byte-arr [neuron-ids]
  "converts the neural pattern to an array of byte"
  (byte-array (map byte (map utility/base32todec (map name neuron-ids))))
  )