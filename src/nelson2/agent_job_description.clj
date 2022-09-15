(ns nelson2.agent-job-description
  (:require [nelson2.neural_processes :as neural-processes] [nelson2.reward-cluster :as cluster] [nelson2.reward-log :as r-log]
            [nelson2.brain :as brain]  [nelson2.extract-data-features :as extract-concept] [nelson2.log :as log]))
"Has the job description of the agent"

(defn neuron-manager [manager]
  "Manage the neurons"
    (neural-processes/flush-neuron (key manager))
    (Thread/sleep @(:neuron-latency brain/params))
    (neural-processes/deactivate-neuron (key manager))
  ;;(log/log (str "Neuron-manager reporting. ID - " (key manager)))
  )
(defn concept-engineer []
  "creates concepts"
  (let [random-tuple (neural-processes/select-random-tuple)]
    (extract-concept/get-structure random-tuple (neural-processes/get-focus)))

  (Thread/sleep @(:concept-engineer-latency brain/params))
  (log/log "Concept-engineer reporting."))

(defn reward-neuron-manager [manager]
  "Manage the reward-neurons"
  (cluster/flush (key manager))
  (Thread/sleep @(:reward-neuron-sleep cluster/params))
  (cluster/deactivate-neuron (key manager))
  (r-log/log (str "Reporting " (key manager)))
  )
