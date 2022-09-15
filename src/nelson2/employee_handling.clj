(ns nelson2.employee-handling (:require [nelson2.brain :as brain] [nelson2.utility :as utility] [nelson2.reward-cluster :as cluster]
                                               [nelson2.agent-job-description :as job] [nelson2.log :as log]))
"consist of a set of agents"

(defn neuron-managers []
  "creates agents that manage individual neurons"
  (map agent @brain/neural-cluster)
  )

(defn reward-neuron-managers []
  (map agent @cluster/personality))

(defn concept-engineers [n]
  (vec (map agent (range n))))

(defn send-neuron-managers-to-work []
  (doseq [manager (neuron-managers)] (send manager (fn [x] (job/neuron-manager x))))
  (Thread/sleep @(:recruiting-latency brain/params)))

(defn send-concept-engineer-to-work []
  (doseq [manager (concept-engineers @(:number-of-concept-engineers brain/params))]
                                                                                    (send manager (fn [_] (job/concept-engineer)))))

(defn send-reward-neuron-managers-to-work []
  (doseq [manager (reward-neuron-managers)] (send manager (fn [x] (job/reward-neuron-manager x)))))

(defn automatically-manage-reward-center []
  "Manage the reward neurons automatically"
  (send-reward-neuron-managers-to-work)
  (Thread/sleep @(:manager-latency cluster/params))
  (cluster/save-neurons @cluster/personality)
  (automatically-manage-reward-center)
  )
(defn reward-cluster-engineer []
  "create the reward center and then connect the brain to it"
  (cluster/create-connect-reward-center)
  (cluster/mutate-cluster)
  (automatically-manage-reward-center)
  )

(defn recruit []
  (pcalls send-concept-engineer-to-work send-neuron-managers-to-work)
  (utility/save-neurons @brain/neural-cluster)
  )

(defn background-recruit []
  "recruit automatically in the background"
  (recruit)
  (background-recruit)
  )