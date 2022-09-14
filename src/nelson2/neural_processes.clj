(ns nelson_clojure.neural_processes
  (:require [nelson_clojure.brain :as brain])
  (:gen-class)
  (:require [nelson_clojure.weight_update :as weight-update]
            [nelson_clojure.log :as log]
            [clojure.edn :as edn] [clojure.data.json :as json][nelson_clojure.utility :as utility])
  (:use [clojure.string]))

"Perform neural processes like thinking, creating neural maps, concept overlap"

(defn deactivate-neuron [neuron-id]
  "Deactivate the supplied neuron"
  (when (not= nil (get @brain/neural-cluster (keyword neuron-id))) (swap! (get (get @brain/neural-cluster (keyword neuron-id)) :state) (fn [_] 0)))
  (log/log (str "Deactivated " (keyword neuron-id)))
  )
(defn activate-neurons [neuron-ids]
  "Activate the supplied neuron"
  (doseq [key neuron-ids] (when (not= nil (key @brain/neural-cluster)) (swap! (:state (key @brain/neural-cluster)) (fn [_] 1))))
  (log/log (str "Activated " neuron-ids))
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
    (doseq [pair pairings] (when-not (= (nth pair 0) (nth pair 1)) (update-coordinates (nth pair 0) (nth pair 1)))
                           (if (connection-exists? (keyword (nth pair 0)) (keyword (nth pair 1)))
                             (do (weight-update/learn @(get (get @brain/neural-cluster (nth pair 0)) :dendrites) (nth pair 1) brain/params))
                             (do
                               (swap! (get (get @brain/neural-cluster (nth pair 0)) :dendrites) (fn [_] (assoc @(get (get @brain/neural-cluster (nth pair 0)) :dendrites) (nth pair 1) (atom 0))))
                               (weight-update/learn @(get (get @brain/neural-cluster (nth pair 0)) :dendrites) (nth pair 1) brain/params)
                               ))
                           )

    )
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

(defn load-json-neurons []
  (let [x (into [] (get-neurons))]
    (println (json/read-str (get x 0) :value-fn (fn [key_ val_] (str (keyword val) ))))
    )
  )
(defn load-neurons []
  "load neurons from file"
  (def tags {'nelson_clojure.brain.skeleton brain/parse-skeleton, 'nelson_clojure.brain.coord brain/handle-coord, 'object brain/handle-object})
   (let [x (map #(try (edn/read-string {:readers tags} %) (catch Exception ex))(get-neurons))] (swap! brain/neural-cluster (fn [_] (apply hash-map (flatten x)))))
  (log/log "Neurons loaded.")
  )

(defn calc-prob [dendrites]
  "Takes a map of dendrites and calculates the excitation probabillity"
  (if (= 0 (count dendrites)) (get brain/params :base-excitation-probability)
                              (apply + (map #(* (deref (get dendrites %)) @(get (get @brain/neural-cluster %) :state) ) (keys dendrites)) )
                              )
  )
(defn flush-neuron [neuron-id]
  "Takes a neuron and calculates the flush probabilities"
  (let [neuron (get @brain/neural-cluster (keyword neuron-id)), dendrites @(get neuron :dendrites)] (when-not (empty? (random-sample (if (= 0 (count (get-active-neurons))) (deref (:base-excitation-probability brain/params)) (calc-prob dendrites)) [1])) (activate-neurons [(keyword neuron-id)]))(log/log (str "excitation probability calculated : " (keys dendrites))))
  )
(defn select-random-tuple []
  "returns only a pair for now"
  (let [tuple [(rand-nth (keys @brain/neural-cluster)) (rand-nth (keys @brain/neural-cluster))]] tuple)
  ;;
  ; (let [tuple (repeatedly (get-focus) (count @brain/neural-cluster))] (println "select random tuple " tuple) (vec (map #((nth % (nth tuple 0)) (nth % (nth tuple 1))) (keys @brain/neural-cluster))))
  ;;
  )