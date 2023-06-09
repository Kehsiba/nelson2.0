(ns nelson2.agent_job_description
  (:require [nelson2.neural_processes :as neural-processes] [nelson2.reward_cluster :as cluster] [nelson2.reward_log :as reward-log]
            [nelson2.brain :as brain]  [nelson2.extract_data_features :as extract-concept] [nelson2.log :as log]))
"Has the job description of the agent"

(defn neuron-manager [manager]
  "Manage the neurons"
    (neural-processes/flush-neuron (key manager))
    (Thread/sleep @(:neuron-latency brain/params))
  "check if the neuron is activated"
    (if (= 1 @(:state (get @brain/neural-cluster (key manager))))
        (do
          (neural-processes/deactivate-neuron (key manager))
          )
        )
  (cluster/connect-residual-neurons)
  (neural-processes/gradual-forgetting (key manager))
  )
(defn concept-engineer []
  "creates concepts"
  (let [random-tuple (neural-processes/select-random-tuple)]
    (extract-concept/get-structure random-tuple (neural-processes/get-focus)))

  (Thread/sleep @(:concept-engineer-latency brain/params)))

(defn reward-neuron-manager [manager]
  "Manage the reward-neurons"
  (cluster/flush (key manager))
  (Thread/sleep @(:reward-neuron-sleep cluster/params))

  (if (= 1 @(:state (get @cluster/personality (key manager))))
    (cluster/deactivate-neuron (key manager))
    )
  ;;(cluster/connect-to-brain)

  )
