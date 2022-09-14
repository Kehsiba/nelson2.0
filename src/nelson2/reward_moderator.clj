(ns nelson_clojure.reward-moderator
  (:require [nelson_clojure.reward-cluster :as cluster]
            [nelson_clojure.reward-log :as log]
            [nelson_clojure.brain :as brain])  (:gen-class))
"calculates the reward and gives feedback to the neuron as if the new pattern increases or decreases the pleasure"
(defn calc-reward []
"calculates the total pleasure obtained from` the orgy"
  (/ (count (cluster/get-live-neurons)) (count @cluster/personality))
  )
(defn moderate-reward [old-val, new-val]
  "decide whether to give a positive or negative feedback"
  )




