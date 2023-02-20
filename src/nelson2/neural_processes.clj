(ns nelson2.neural_processes
  (:require [nelson2.brain :as brain] [nelson2.reward-log :as reward-log])
  (:gen-class)
  (:require [nelson2.weight_update :as weight-update]
            [nelson2.log :as log]
            [clojure.edn :as edn]
            [nelson2.utility :as utility])
  (:use [clojure.string]))

"Perform neural processes like thinking, creating neural maps, concept overlap"

(defn deactivate-neuron [neuron-id]
  "Deactivate the supplied neuron"
  (when (not= nil (get @brain/neural-cluster neuron-id)) (swap! (:state (get @brain/neural-cluster neuron-id)) (fn [_] 0)))
  (log/log (str "Deactivated " (keyword neuron-id)))
  )
(defn activate-neurons [neuron-ids]
  "Activate the supplied neuron"
  (doseq [key neuron-ids] (when (not= nil (key @brain/neural-cluster))(if (= 0 @(:state (get @brain/neural-cluster key)))
                                                                         (do
                                                                          (swap! (:state (get @brain/neural-cluster key)) (fn [_] 1))
                                                                          ;(println "activated " key)
                                                                          (log/log (str "Activated " key))
                                                                          ))))

  )

(defn update-radius [radius]
  "change the radius of the child neuron"
  (* radius 0.9)
  )

(defn get-radius [parent-x parent-y parent-z child-x child-y child-z]
  (Math/sqrt (+ (Math/pow (- parent-x child-x) 2) (Math/pow (- parent-y child-y) 2) (Math/pow (- parent-z child-z) 2))))

(defn calc-cos-theta [child-x child-y child-z parent-x parent-y parent-z]
  (/ (- child-z parent-z) (get-radius parent-x parent-y parent-z child-x child-y child-z)))

(defn calc-cos-phi [child-x child-y child-z parent-x parent-y parent-z cos-theta]
  (/(- child-y parent-y) (* (get-radius parent-x parent-y parent-z child-x child-y child-z) (Math/sqrt (- 1 (Math/pow cos-theta 2)))))
  )
(defn update-coordinates [parent-neuron child-neuron]
  "The directed graph is from the parent neuron to the child neuron"
  (let [parent-x @(get (get (get @brain/neural-cluster parent-neuron) :coordinate) :x), parent-y @(get (get (get @brain/neural-cluster parent-neuron) :coordinate) :y), parent-z @(get (get (get @brain/neural-cluster parent-neuron) :coordinate) :z)
        child-x @(get (get (get @brain/neural-cluster child-neuron) :coordinate) :x), child-y @(get (get (get @brain/neural-cluster child-neuron) :coordinate) :y), child-z @(get (get (get @brain/neural-cluster child-neuron) :coordinate) :z)
        cos-theta (calc-cos-theta child-x child-y child-z parent-x parent-y parent-z),
        cos-phi (calc-cos-phi child-x child-y child-z parent-x parent-y parent-z cos-theta),
        r (update-radius (get-radius parent-x parent-y parent-z child-x child-y child-z ))
        ]
    (swap! (get (get (get @brain/neural-cluster child-neuron) :coordinate) :x) (fn [_] (* r (Math/sqrt (- 1 (Math/pow cos-theta 2))) (Math/sqrt (- 1 (Math/pow cos-phi 2))))))
    (swap! (get (get (get @brain/neural-cluster child-neuron) :coordinate) :y) (fn [_] (* r (Math/sqrt (- 1 (Math/pow cos-theta 2))) cos-phi)))
    (swap! (get (get (get @brain/neural-cluster child-neuron) :coordinate) :z) (fn [_] (* r  cos-phi)))
    )
  )
(defn connection-exists? [parent child]
  "check if the connection exists between parent and child"
  (if (= nil (find (get (find @brain/neural-cluster parent) :dendrites) child)) false true))

(defn get-neuron [neuron-id]
  (let [neuron (get @brain/neural-cluster neuron-id)] neuron)
  )
(defn create-neural-map [neuron-ids]
  "update neurons"
  "Takes a set of ids and then updates the weights of the neurons"
  (activate-neurons neuron-ids)
  ;;(println @brain/neural-cluster)
  "update the coordinates of the neurons"
  (let [pairings (partition 2 (interleave neuron-ids (rest neuron-ids)) )]
    (doseq [pair pairings]
                           (if (connection-exists? (keyword (nth pair 0)) (keyword (nth pair 1)))
                             (do (weight-update/learn @(get (get @brain/neural-cluster (nth pair 0)) :dendrites) (nth pair 1) brain/params))
                             (do
                               (swap! (get (get @brain/neural-cluster (nth pair 0)) :dendrites) (fn [_] (assoc @(get (get @brain/neural-cluster (nth pair 0)) :dendrites) (nth pair 1) (atom 0))))
                               (weight-update/learn @(get (get @brain/neural-cluster (nth pair 0)) :dendrites) (nth pair 1) brain/params)
                               ))
                           )

    )
  )
(defn gradual-forget-dendrite [dendrite]
  "update the weight of the dendrite"

  (if (< 0 @(first (vals dendrite)))
    (when (not= nil dendrite)
      (swap! (first (vals dendrite)) (fn [_] (weight-update/forget-update @(first (vals dendrite)) brain/params)   )) )
     (reset! (first (vals dendrite)) 0)
    )
  (when (not= nil dendrite)
    (swap! (first (vals dendrite)) (fn [_] (weight-update/forget-update @(first (vals dendrite)) brain/params)   )) )
  )
(defn gradual-forgetting [neuron-id]
  "gradually forget the weights"
  "list all dendrites"
  (let [dendrites @(:dendrites (get @brain/neural-cluster neuron-id))]
    "update each weight"

    (doall (map #(gradual-forget-dendrite (apply assoc {} %)) dendrites)))
  (log/log (str "Forgetting : " neuron-id))
  )
(defn forget-weight [parent child]
  "Forget the connection between the parent and child"
  (swap! (get (get (get @brain/neural-cluster (keyword parent)) :dendrites) (keyword child)) (fn [_] 0))
  )
(defn get-focus []
  "decide the focus for the concept extraction process"
  ;;(let [focus (rand-int @(:concept-cap brain/params))] (if (and (= focus 0) (= focus 1)) (get-focus) focus))
  2
  )
(defn get-active-neurons []
  "return the set of active neurons"
  (filter (fn [neuron] (if (= 1 @(:state (get neuron 1))) true false)) @brain/neural-cluster)
  )
(defn get-neuron-files []
  (map #(apply str (filter (fn [_] (ends-with? % ".neuron")) (when (.isFile %) (.getName %)))) (file-seq (clojure.java.io/file "neuron-data/"))))

(defn get-neurons []
  "Loads all the neurons and returns a set of neurons"
  (map  (fn [neuron-file] (slurp (str "neuron-data/" neuron-file))) (remove empty? (set (get-neuron-files))) )
  )

(defn get-reward-neuron-files []
  (map #(apply str (filter (fn [_] (ends-with? % ".neuron")) (when (.isFile %) (.getName %)))) (file-seq (clojure.java.io/file "reward-neuron/"))))

(defn remove-neuron [neuron-id]
  "Removes the neuron from the brain"

  (swap! brain/neural-cluster (fn [_] (dissoc @brain/neural-cluster neuron-id)))
  (println (str "Deleted neuron " neuron-id))
  (log/log (str "Neuron deleted " neuron-id))
  )
(defn isLonely? [neuron]
  "takes a neuron and checks if the dendrites are strong enough"
  (let [dendrites (vals @(:dendrites (into {} (vals (apply array-map neuron)))))]
    (> @(:dendrite-strength-threshold brain/params) (utility/average (map #(deref %) dendrites)))
      )
  )
(defn collect-garbage []
  "Deletes isolated neurons"
  (doseq [list  (filter (fn [x] (isLonely? x)) @brain/neural-cluster)]
    (remove-neuron (get list 0)))
    (println "Garbage collection completed")
  )
(defn get-reward-neurons []
  "Loads all the neurons and returns a set of neurons"
  (map  (fn [neuron-file] (slurp (str "reward-neuron/" neuron-file))) (remove empty? (set (get-reward-neuron-files))) )
  )

(defn load-neurons []
  "load neurons from file"
  (def tags {'nelson2.brain.skeleton brain/parse-skeleton, 'nelson2.brain.coord brain/handle-coord, 'object brain/handle-object})
   (let [x (map #(try (edn/read-string {:readers tags} %) (catch Exception ex (println (str "Exception caught - " ex))))(get-neurons))] (swap! brain/neural-cluster (fn [_] (apply hash-map (flatten x)))))
  (log/log "Neurons loaded.")
  )

(defn calc-prob [neuron-id dendrites]
  "Takes a map of dendrites and calculates the excitation probabillity"
  (if (= 0 (count dendrites)) (* @(:state (get @brain/neural-cluster neuron-id)) @(get brain/params :base-excitation-probability))
    (/ (apply + (map #(* (deref (get dendrites %)) @(get (get @brain/neural-cluster %) :state) ) (keys dendrites)) ) (count (keys dendrites)))))

(defn get-neuron-priority [neuron-id]
"get the neural probability"
  "if state = 0 then probability is state*base-excitation-probability"
  "if state = 1 then calculate the probability"
  (if (= 0 @(:state (get @brain/neural-cluster neuron-id)))
    (
     (* @(:state (get @brain/neural-cluster neuron-id)) @(:base-excitation-probability brain/params))
     )
    (calc-prob neuron-id @(:dendrites (get @brain/neural-cluster neuron-id)))
    )
  )
(defn flush-neuron [neuron-id]
  "Takes a neuron and flushes it"
  (let [priority (get-neuron-priority neuron-id)]
    (when-not (empty? (random-sample priority [1]))
      ;(println "flushing = " neuron-id " to be = "priority)
      (activate-neurons [neuron-id]))
    (log/log (str "excitation probability calculated for " neuron-id " to be " priority))
    priority
    )
  )
(defn select-random-tuple []
  "returns only a pair for now"
  (let [tuple [(rand-nth (keys @brain/neural-cluster)) (do (Thread/sleep @(get brain/params :tuple-sample-latency)) (rand-nth (keys @brain/neural-cluster)))]] tuple)
  ;;
  ; (let [tuple (repeatedly (get-focus) (count @brain/neural-cluster))] (println "select random tuple " tuple) (vec (map #((nth % (nth tuple 0)) (nth % (nth tuple 1))) (keys @brain/neural-cluster))))
  ;;
  )