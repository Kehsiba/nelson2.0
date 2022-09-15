(ns nelson2.logic-remember (:require [nelson2.utility :as utility] [nelson2.brain :as brain]))
"sample from the brain"


(defn get-probable-concepts [neuron-id]
  "Takes a neuron-id and then finds the set of neurons which could be the concept"
  (filter (fn [neuron] (when (> (- (utility/concept-level? neuron) (utility/concept-level? neuron-id)) 1) true)) (keys @brain/neural-cluster))
  )

(defn get-concept [neuron-id]
  "takes a concept and finds the higher concept"
  (let [probable-concepts (get-probable-concepts neuron-id)]
    "check if any of the probable concepts have neuron-id as their sub-concept"
    (filter (fn [neuron] (when (not= nil (find @(:dendrites (get @brain/neural-cluster neuron)) neuron-id)) true)) probable-concepts)))

(defn remember-all-concepts [neuron-ID]
  "Remember all concepts of neuron-ID"
  (let [concepts (atom (get-concept neuron-ID))] (when (not= 0 (count @concepts)) (doseq [concept @concepts]
                                                                            (swap! concepts (fn [_] (flatten (merge @concepts (remember-all-concepts concept))))))) @concepts))


