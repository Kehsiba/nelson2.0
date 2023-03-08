(ns nelson2.extract_data_features  (:require
                                     [nelson2.utility :as utility]
                                     [nelson2.log :as log])
  (:require
    [nelson2.brain :as brain]
    [nelson2.neural_processes :as neural-processes] )
  (:use [clojure.set :only (intersection)])  (:gen-class))
"takes the list of neuron ids and then extracts the features in the data"

(defn validate-pair [neuron-pair]
  "Checks if the two neurons are connected"
  (if (= nil (find @(get (get @brain/neural-cluster (nth neuron-pair 0)) :dendrites) (nth neuron-pair 1))) false true)
  )
(defn connect-concepts [tuple]
  "Takes the two concepts and creates a concept out of that"
  (if (= (nth tuple 0) (nth tuple 1))
    (neural-processes/create-neural-map tuple)
    (neural-processes/create-neural-map (reverse tuple)))
  (log/log (str "concepts connected " (vec tuple)))
  )
(defn concept-overlap-exists? [tuple]
  (when (not= 0 (count (intersection (set (keys @(:dendrites (get @brain/neural-cluster (nth tuple 0)))))
                             (set (keys @(:dendrites (get @brain/neural-cluster (nth tuple 1))))))))
    (connect-concepts tuple)
    )
  )
(defn validate-tuple [tuple]
  "check if the set of neurons forms a connected graph"
  (if (= 2 (count tuple)) (validate-pair (seq tuple)) (and (validate-pair (take 2 (seq tuple))) (validate-tuple (rest (seq tuple)))))
)

(defn calc-concept [neuron-ids]
  "combines the concepts and returns a bigger concept"
  ;;(println "neuron-ids = " neuron-ids)
  ;;(println "....... " (utility/compress-data (vec (map #(utility/base32todec (name %)) neuron-ids))))
  (let [concept (utility/dec2base32 (utility/compress-data (vec (map #(utility/base32todec (name %)) neuron-ids))))]
    ;;(println "concept = " concept)
    (when (< (utility/concept-level? concept) @(:concept-cap brain/params))
      (swap! brain/neural-cluster (fn [_] (merge @brain/neural-cluster (into {} (brain/init [(keyword concept)])))))
      (neural-processes/create-neural-map (merge neuron-ids (keyword concept)))
      (log/log (str "Concept created with " (into [] neuron-ids)))
      )
    )
  )
(defn create-graph-topologies [neuron-ids focus]
  (partition focus (apply interleave (repeatedly focus (partial shuffle neuron-ids))))
  )
(defn connect-concept [neuron-ids]
  "check if the neurons contain concepts that overlap"
  (if (= 2 (count neuron-ids)) (concept-overlap-exists? neuron-ids) (connect-concept (rest neuron-ids)))
  )
(defn extract-concept [neuron-ids]
  "takes a set of neurons and then combines them to create another neuron called concept"
  "if the weight between the neurons is 0 then it is not included in the concept"
  (doseq [tuple neuron-ids] (if (validate-tuple tuple)
                              (let [concept (calc-concept tuple)]  ))
                            "check for concept overlaps"
                            "if the neurons are the same then skip"
                            ()
                            (connect-concept tuple)
  ) )
(defn get-structure [neuron-ids focus]
  "neuron-ids refers to the neurons within which we want to find the structure"
  "focus refers to how deep you want to focus i.e. clusters of 2, clusters of 3..."

  (let [concepts (extract-concept (create-graph-topologies neuron-ids focus))] concepts
   "Activate concepts"
                                                                               (neural-processes/activate-neurons (into [] concepts))
                                                                               )
  )
