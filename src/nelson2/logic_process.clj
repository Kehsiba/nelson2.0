(ns nelson2.logic_process
  (:require [nelson2.log :as log]
            [nelson2.utility :as utility] [nelson2.neural_processes :as neural-processes] [nelson2.log :as log]
           [nelson2.logic-remember :as logic-remember] [nelson2.reward-moderator :as reward-moderator] [nelson2.brain :as brain]))
"Take the brain and the reward center"
"Given a fixed set of neurons which remain excited- make all possible conclusions"
"extract the concept neurons of the given neurons first"
(def params {:logic-thread-timeout (atom 1000) :logic-thread-count-sup (atom 10)})

(defn deactivate-all [neuron-ids]
  "takes a set of neurons and deactivates them"
  (comment
    (map #(add-watch (:state (get @brain/neural-cluster %)) % (fn [key agent old-state new-state]
                                                                (when (= @new-state 0) (neural-processes/activate-neurons [key])))) neuron-ids))
  (map neural-processes/deactivate-neuron neuron-ids)
  (log/log (str "Deactivated logic tree: " neuron-ids))
  )


(defn activate-all [neuron-ids]
  "takes a set of neurons and keeps them activated all the time"
  (comment
  (map #(add-watch (:state (get @brain/neural-cluster %)) % (fn [key agent old-state new-state]
                                                          (when (= @new-state 0) (neural-processes/activate-neurons [key])))) neuron-ids))
  (neural-processes/activate-neurons neuron-ids))

(defn remove-watchers [neuron-ids]
  "remove all the watchers on the neurons at the end of logical inference"
  (map #(remove-watch (agent %) (keyword %)) neuron-ids)
  )

(defn infer-concept-neurons [neuron-ids]
  "takes a set of neurons and identifies the concepts relevant for the series"
  "the neuron-ids stay activated during the thinking process so just sample from the brain"
  (let [inferred-concepts (set (flatten (apply conj (map #(logic-remember/remember-all-concepts %) neuron-ids))))]
    (log/log (str "Concepts inferred for : " neuron-ids))
    inferred-concepts))

(defn activate-concept-tree [neuron-ids]
  "take the set of neurons and then activate the entire concept hierarchy"
  (activate-all neuron-ids))

(defn deactivate-concept-tree [neuron-ids]
  "take the set of neurons and then deactivate the entire concept hierarchy"
  (activate-all neuron-ids))
(defn filter-dendrites [neuron-id]
  "get dendrites of the same concept level"
  (filter (fn [x] (if (= (Math/round (utility/concept-level? x)) (Math/round (utility/concept-level? neuron-id))) true false))
          (keys @(:dendrites (get @brain/neural-cluster neuron-id)))))

(defn thread-length-regulator [logic-thread]
  "decides how long the logic thread should be"
  (if (not= (count @logic-thread) @(:logic-thread-count-sup params)) true false))

(defn construct-logic-thread [concept-neuron-id, logic-thread]
  "Takes the initial concept neuron and returns a directed graph of concepts"
  (when (thread-length-regulator logic-thread)
    (let [filtered-concepts (filter-dendrites concept-neuron-id)]
      (if (= nil filtered-concepts)
        ("no more concepts to activate" concept-neuron-id)
        (let [live-intersections (vec (clojure.set/intersection (set (keys (neural-processes/get-active-neurons))) (set filtered-concepts)))]
          (if (not= 0 (count live-intersections))
            (let [selection (rand-nth live-intersections)]
              (swap! logic-thread (fn [_] (conj @logic-thread selection)))
              (Thread/sleep @(:logic-thread-timeout params))
              (construct-logic-thread selection logic-thread)))
          logic-thread
          ))
      ))
  )

(defn start-logic-thread [concept-neuron-id]
  "call construct logic thread"
  (construct-logic-thread concept-neuron-id (atom [concept-neuron-id]))
  )

(defn expand-logic-thread [logic-thread]
  "Given the logic threads find the lower concept representations i.e the byte representation of the concepts"
  "all the neurons are at the same concept level"
  (let [concept-level (mapv #(int (Math/floor (utility/concept-level? %))) logic-thread)]
    (println (str "Concept levels "  concept-level))
    (mapv #(keyword (utility/dec2base32 %)) (flatten (map #(utility/de-compress [(utility/base32todec (name %))] (first concept-level)) logic-thread)))))

(defn logical-inference [neuron-ids]
  "logically infer everything from the data"
  (let [inferred-concepts (infer-concept-neurons neuron-ids), outcome-list {}]
    (println (str "inferred neurons: " inferred-concepts))
    "filter the dendrites of each concept in inferred-concepts and start the logic thread for each concept"
    (let [report (atom [])]
      (doseq [concept inferred-concepts]
        "activate all the nodes in the logic thread"
        (let [logic-thread (start-logic-thread concept)]
          (println (str "logic thread: " concept))
          (activate-concept-tree @logic-thread)
          "calculate the reward for each logic-thread"
          (let [thread-table (merge outcome-list (zipmap [:logic-thread :reward] [logic-thread (reward-moderator/calc-reward)]))]

            (comment "remove the watchers"
            (remove-watchers logic-thread))

            "deactivate logic thread"
              (deactivate-all @logic-thread)

              (println (str "Calculated reward: " @logic-thread))
            (swap! report (fn [_] (conj @report thread-table)))
            )))
            (log/log "Logical inference report generated")
            report))
  )

(defn sample-spontaneous-mode []
  "This is called when the mode is for the AI to come up with something"
   (spit "neural_network_output.txt" (mapv #(logical-inference [%]) (keys (neural-processes/get-active-neurons))))
  (log/log "Response obtained for active neurons.")
  )

(defn sample-response-mode [question]
  "This is called when the mode is to get a response to a question"


  )