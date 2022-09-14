(ns nelson-clojure.logic_process
  (:require [nelson_clojure.log :as log]
            [nelson_clojure.utility :as utility] [nelson_clojure.neural_processes :as neural-processes] [nelson_clojure.log :as log]
           [nelson-clojure.logic-remember :as logic-remember] [nelson_clojure.reward-moderator :as reward-moderator] [nelson_clojure.brain :as brain]))
"Take the brain and the reward center"
"Given a fixed set of neurons which remain excited- make all possible conclusions"
"extract the concept neurons of the given neurons first"
(def params {:logic-thread-timeout (atom 300) :logic-thread-count-sup (atom 100)})

(defn activate-all [neuron-ids]
  "takes a set of neurons and keeps them activated all the time"
  (map #(add-watch (:state (get @brain/neural-cluster %)) % (fn [key agent old-state new-state]
                                                          (when (= @new-state 0) (neural-processes/activate-neurons [key])))) neuron-ids)
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
  "sample multiple times and prepare a list of outcome vs frequency"
  (let [concept-level (int (Math/floor (utility/concept-level? (first logic-thread))))]
    (map #(keyword (utility/dec2base32 %)) (apply merge (map #(utility/de-compress [(utility/base32todec (name %))] concept-level)logic-thread)))))

(defn logical-inference [neuron-ids]
  "logically infer everything from the data"
  (let [inferred-concepts (infer-concept-neurons neuron-ids), outcome-list {}]
    "filter the dendrites of each concept in inferred-concepts and start the logic thread for each concept"
    (let [report (atom [])]
      (doseq [concept inferred-concepts]
        "activate all the nodes in the logic thread"
        (let [logic-thread (start-logic-thread concept)]
          (activate-concept-tree @logic-thread)
          "calculate the reward for each logic-thread"
          (let [thread-table (merge outcome-list (zipmap [:logic-thread :reward] [logic-thread (reward-moderator/calc-reward)]))]
            "remove the watchers"
            (remove-watchers logic-thread)

            (swap! report (fn [_] (conj @report thread-table)))
            )))
            report))
  (log/log "Logical inference report generated"))

(defn sample-spontaneous-mode []
  "This is called when the mode is for the AI to come up with something"
  (spit "neural_network_output.txt" (vec (map #(logical-inference [%]) (keys (neural-processes/get-active-neurons)))))
  (log/log "Response obtained for active neurons.")
  )

(defn sample-response-mode [question]
  "This is called when the mode is to get a response to a question"


  )