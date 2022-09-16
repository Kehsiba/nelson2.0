(ns nelson2.reward-cluster
  (:gen-class)(:use [clojure.string :only (index-of)])
  (:require [nelson2.reward-log :as reward-log]
            [clojure.java.io :as io]
            [nelson2.brain :as brain]
            [clojure.edn :as edn]
            [nelson2.neural_processes :as neural_processes]
            [clojure.set :as set]))

(defrecord reward-neurons [state connections] :load-ns true)
(def personality (atom {}))
(def params {:number-of-reward-neurons (atom 100), :base-neuron-interest (atom 1),
             :reward-neuron-sleep (atom 1000), :manager-latency (atom 2000)})

(defn get-random-weights []
  (rand))

(defn create-neuron-file [id]
  "create a file with the supplied id"
  (spit (str "reward-neuron/" (name id) ".neuron") "Fuck Google for now")
  (reward-log/log (str "Created file " (name id))))

(defn create-initial-neuron [id]
  "create an initial neuron"
  (let [neuron (hash-map (keyword id) (reward-neurons. (atom 0) (atom {})))]
    (when-not (.exists (io/file (str "reward-neuron/" id ".neuron"))) (create-neuron-file id))
    (reward-log/log (str "Created neuron : " id)) neuron))

(defn create-neuron-relations [neuron-key keys-of-other-neurons]
  (swap! (:connections (get @personality neuron-key)) (fn [_] (apply merge (map #(hash-map % (atom 0)) keys-of-other-neurons)))))

(defn get-live-neurons []
  "return the set of active neurons"
  (keys (filter (fn [neuron] (if (= 1 @(:state (get neuron 1))) true false)) @personality))
  )

(defn handle-object [x]
  (def tags {'object handle-object})
  (atom (:val (get (edn/read-string {:readers tags} (str x)) 2))))

(defn parse-reward-personality [x]
  (def tags {'object handle-object})
  (reward-neurons. (:state x) (:connections x) )

  )
(defn load-reward-neurons []
  "load reward neurons"
  (let [x (map #(try (edn/read-string {:readers {'nelson2.reward_cluster.reward-neurons parse-reward-personality, 'object brain/handle-object}} %)
                     (catch Exception ex (println (str "Exception caught - " ex))))
               (neural_processes/get-reward-neurons))]
    (swap! personality (fn [_] (apply hash-map (flatten x))))
    "formatting the data structure"
    (swap! personality (fn [_] (merge (apply set/union (keys @personality)) (apply set/union (vals @personality)))))
    )
  (reward-log/log "Neurons loaded.")
  )


(defn create-reward-cluster []
  "Takes the personality atom and updates the relationship between themselves"
  (doseq [neuron @personality] (create-neuron-relations (key neuron) (keys (dissoc @personality (key neuron)))))
  (reward-log/log "Reward center created."))

(defn create-neuron [neuron-names]
  "create the neurons"
  (when-not (.exists (io/file "reward-neuron")) (.mkdir (io/file "reward-neuron")))
  (swap! personality (fn [_] (into {} (map create-initial-neuron (read-string (str " " neuron-names)))))))

(defn mutate [list]
  (doseq [store-key (keys list)] (swap! (get list store-key) (fn [_] (get-random-weights)))))

(defn mutate-cluster []
  "mutate the weights of the cluster"
  (doseq [neuron-key (keys @personality)] (mutate (deref (:connections (get @personality neuron-key)))) )
  (reward-log/log "New personality generated."))

(defn connect-reward-center-to-brain [neuron-key]
  "connect to the neurons in the brain"
  (swap! (:connections (get @personality neuron-key)) (fn [_]
                                                       (apply merge (merge (map #(hash-map % (atom 0))
                                                                                (keys @brain/neural-cluster))
                                                                           @(:connections (get @personality neuron-key))))))
  (reward-log/log (str "Connected to brain : " neuron-key)))

(defn connect-to-brain []
  "connect all the neurons to the brain"
  (doseq [key (keys @personality)] (connect-reward-center-to-brain key) )
  (reward-log/log "All neurons connected to brain"))

(defn save-neurons [cluster]
  "save the neurons to their files"
  (when-not (= cluster {}) (spit (str "reward-neuron/" (name (ffirst cluster)) ".neuron" ) (apply array-map (first cluster)))
                           (save-neurons (into {} (rest cluster)))))

(defn deactivate-neuron [neuron-id]
  "Deactivate the supplied neuron"
  (when (not= nil (get @personality (keyword neuron-id))) (swap! (get (get @personality (keyword neuron-id)) :state) (fn [_] 0)))
  (reward-log/log (str "Deactivated " (keyword neuron-id))))

(defn activate-neurons [neuron-ids]
  "Activate the supplied neurons"
  (doseq [key neuron-ids] (when (not= nil (key @personality)) (swap! (:state (key @personality)) (fn [_] 1))))
  (reward-log/log (str "Activated " neuron-ids)))

(defn validate-neuron-id [id]
  "checks if the neuron belongs to the neural cluster or the reward cluster"
  "True : belongs to neural cluster"
  "False : belongs to reward cluster"
  (if (= 0 (index-of (name id) "reward")) false true )
  )
(defn interest-formula [connections]
  "given the connections calculates the interest using a formula"
  (/ (apply + (map #(* (if (validate-neuron-id %) (deref (:state (get @brain/neural-cluster %))) (deref (:state (get @personality %))))
                       (deref (get connections %))) (keys connections))) (count (keys connections)))
  )

(defn calc-interest [connections]
  "calculates the interest given the connections"
  (if (= 0 (count connections))
    @(:base-neuron-interest params)
    (let [interest (interest-formula connections)] interest)))

(defn calc-interest-of-neuron [neuron-id]
  "calculates the interest of the neuron to participate in the excitement"
  (when-not (= 1 @(:state (get @personality (keyword neuron-id))))
    (let [interest (calc-interest @(:connections (get @personality (keyword neuron-id))))]
      (reward-log/log (str "interest calculated for " neuron-id " to be = " interest))
      interest)))

(defn flush [neuron-id]
  "Flush the given neuron"
  (let [state (random-sample (if (= 0 (count (get-live-neurons))) (deref (:base-neuron-interest params)) (calc-interest-of-neuron neuron-id)) [1])]
    (when (= (first state) 1) (activate-neurons [neuron-id]))
    )
  (reward-log/log (str "Flushed neuron :- " neuron-id))
  )

(defn create-connect-reward-center []
  (let [neurons (create-neuron (vec (map #(str "reward-" %) (rest (range @(:number-of-reward-neurons params))))))]
      (swap! personality (fn [_] neurons))
      (create-reward-cluster)
      (reward-log/log "Reward cluster created.")
      (connect-to-brain)
      (reward-log/log "Reward cluster connected to brain.")
    )
  )