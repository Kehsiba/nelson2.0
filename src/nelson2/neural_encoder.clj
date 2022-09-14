(ns nelson_clojure.neural-encoder
  (:require [nelson_clojure.utility :as utility]
            [nelson_clojure.neural_processes :as neural-processes]
            ) (:gen-class))

"Takes the data bytestream and then creates a neural graph"

(defn create_sequence [byte-array]
  "the byte array is the data array. returns a subsequence with the first element included"
  (let [sub-seq (distinct (apply concat (map #(partition-all % byte-array) (rest (range (inc (count byte-array)))))))] sub-seq))
(defn split-data [byte-vector]
  "The byte vector consists of data as a byte array"
  "Split the byte array into two parts and ten create labels for each of them."
  (let [store (map create_sequence (map #(nthrest (apply list byte-vector) %) (range (count byte-vector))))] (distinct (apply concat store)) store))
(defn split-byteArray-unique [byte-vector]
  "The actual function that splits the byte array into all possible ordered pieces"
  (let [splits (distinct (apply concat (split-data byte-vector)))] splits))

(defn get-labels [byte-array]
  "create the labels for the data array and return the keys of the neurons that need to be activated"
  (let [neurons (map keyword (map utility/dec2base32 byte-array))] neurons)
  )

