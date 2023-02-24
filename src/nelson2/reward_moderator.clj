(ns nelson2.reward_moderator
  (:require [nelson2.reward_cluster :as cluster]
            [nelson2.reward_log :as log]
            [nelson2.brain :as brain])  (:gen-class))
"calculates the reward and gives feedback to the neuron as if the new pattern increases or decreases the pleasure"
(defn calc-reward []
"calculates the total pleasure obtained from` the orgy"
  (/ (count (cluster/get-live-neurons)) (count @cluster/personality))
  )
(defn maximize-reward [iteration-number]
  "given the neural pattern maximize the reward center"
  (let [reward (calc-reward), temp @cluster/personality]
    (if (= iteration-number 0)
      (calc-reward)
      (do
        (if (< reward (do (cluster/mutate-cluster) (calc-reward)))
          (println "swapping ")
          (swap! cluster/personality (fn [_] temp))
        )
      (println "iteration = " iteration-number "reward = " (calc-reward))
      (maximize-reward (dec iteration-number)))
      )
    )
  )




