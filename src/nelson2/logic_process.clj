(ns nelson2.logic_process
  (:require [nelson2.log :as log]
            [nelson2.utility :as utility] [nelson2.neural_processes :as neural-processes] [nelson2.log :as log]
           [nelson2.logic_remember :as logic-remember] [nelson2.reward_moderator :as reward-moderator] [nelson2.text_input_interface :as text-input-interface] [nelson2.brain :as brain]))
"Take the brain and the reward center"
"Given a fixed set of neurons which remain excited- make all possible conclusions"
"extract the concept neurons of the given neurons first"


"logic-thread-timeout :- time between successive logic threads that will be constructed"
"logic-thread-count-sup :- maximum number of neuron-ids in a logic thread"
"temporal-correlation-delay :- temporal between two correlated events"
(def params {
             :logic-thread-timeout (atom 100)
             :logic-thread-count-sup (atom 10)
             :temporal-correlation-delay (atom 5000)})
(defn deactivate-all [neuron-ids]
  "takes a set of neurons and deactivates them"
  (comment
    (map #(add-watch (:state (get @brain/neural-cluster %)) % (fn [key agent old-state new-state]
                                                                (when (= @new-state 0) (neural-processes/activate-neurons [key])))) neuron-ids))
  (map neural-processes/deactivate-neuron neuron-ids)
  (log/log (str "Deactivated logic tree: " neuron-ids))
  )
(defn causal-correlate-delay-regulator []
  "control the time delay for temporal correlation"
  (rand 10)
  )
(defn causally-correlate [time-interval]
  "Correlate causal events separated by time-interval"
  (let [initial-excitation (vec (keys (neural-processes/get-active-neurons)))]
    (Thread/sleep time-interval)
    (let [final-excitation (vec (keys (neural-processes/get-active-neurons)))]
      "connect initial-excitation to final-excitation"
      (neural-processes/create-neural-map (flatten (conj initial-excitation final-excitation)))
      (log/log (str "Causally correlating : " (vec (flatten (conj initial-excitation final-excitation)))))
      )

    )
  )

(defn auto-temporal-corr []
  (causally-correlate (causal-correlate-delay-regulator))
  (Thread/sleep @(:temporal-correlation-delay params))
  (pcalls auto-temporal-corr)
  )
(defn auto-temporal-corr1 []
  (auto-temporal-corr)

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
(defn filter-dendrites [neuron-id]
  "get dendrites of the same concept level"
  (filter (fn [x] (if (= (utility/concept-level? x) (utility/concept-level? neuron-id)) true false))
          (keys @(:dendrites (get @brain/neural-cluster neuron-id)))))

(defn thread-length-regulator [logic-thread]
  "decides how long the logic thread should be"
  (if (not= (count @logic-thread) @(:logic-thread-count-sup params)) true false))

(defn select-nth-logic-element [live-intersections]
  "chooses a random logic element"
  "implement a greedy algorithm that maximizes reward locally"
  (rand-nth live-intersections)
  )
(defn construct-logic-thread [concept-neuron-id, logic-thread]
  "Takes the initial concept neuron and returns a directed graph of concepts"
  (when (thread-length-regulator logic-thread)
    (let [filtered-concepts (filter-dendrites concept-neuron-id)]
      (if (= nil filtered-concepts)
        ("no more concepts to activate" concept-neuron-id)
        (let [live-intersections (vec (clojure.set/intersection (set (keys (neural-processes/get-active-neurons))) (set filtered-concepts)))]
          (if (not= 0 (count live-intersections))
            (let [selection (select-nth-logic-element live-intersections)]
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
  ;;(println "logic-thread = " logic-thread)
  (mapv #(keyword (utility/dec2base32 %)) (flatten (map #(utility/base32todec (name %)) logic-thread)))
  )

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
          (let [thread-table (merge outcome-list (zipmap [:logic-thread :reward] [@logic-thread (reward-moderator/calc-reward)]))]

            "deactivate logic thread"
            (deactivate-all @logic-thread)

            ;;(println (str "Report: " @report))
            (swap! report (fn [_] (conj @report thread-table)))
            )))
      (log/log "Logical inference report generated"
               )
      @report))
  )


(defn sample-spontaneous-mode []
  "This is called when the mode is for the AI to come up with something"
  (log/log "initiating logical report")
  (let [x (mapv #(logical-inference [%]) (keys (neural-processes/get-active-neurons)))]
    (spit "neural_network_output.txt" (prn-str x))
   )

  (log/log "Response obtained for active neurons."))

(defn sample-response-mode [question]
  "This is called when the mode is to get a response to a question"
  (println "question = " question)
   "parse the input"
  (text-input-interface/parse-text question)
  "get the response"
  (let [x (mapv #(logical-inference [%]) (keys (neural-processes/get-active-neurons)))]
    (spit "neural_network_response.txt" (prn-str x))
    )
  )