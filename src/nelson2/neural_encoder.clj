(ns nelson2.neural-encoder
  (:require [nelson2.utility :as utility]
            [nelson2.neural_processes :as neural-processes]
            ) (:gen-class))

"Takes the data bytestream and then creates a neural graph"
(defn get-labels [byte-array]
  "create the labels for the data array and return the keys of the neurons that need to be activated"
  (let [neurons (map keyword (map utility/dec2base32 byte-array))] neurons)
  )

