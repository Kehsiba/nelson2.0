(ns nelson2.reward-moderator
  (:require [nelson2.reward-cluster :as cluster]
            [nelson2.reward-log :as log]
            [nelson2.brain :as brain])  (:gen-class))
"calculates the reward and gives feedback to the neuron as if the new pattern increases or decreases the pleasure"
(defn calc-reward []
"calculates the total pleasure obtained from` the orgy"
  (/ (count (cluster/get-live-neurons)) (count @cluster/personality))
  )
(defn moderate-reward [old-val, new-val]
  "decide whether to give a positive or negative feedback"
  )




