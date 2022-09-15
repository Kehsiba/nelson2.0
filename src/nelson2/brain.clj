(ns nelson2.brain
  (:require [clojure.java.io :as io]
            [nelson2.utility :as utility]
            [nelson2.log :as log]
            [clojure.edn :as edn]) (:gen-class))

(def params {:recruiting-latency (atom 1000), :number-of-concept-engineers (atom 4), :neuron-latency (atom 500), :concept-engineer-latency (atom 300), :encoding-base (atom 256),
             :concept-cap (atom 10),:base-excitation-probability (atom 0.5), :time-interval (atom 0.3), :learning-timescale (atom 0.7), :forgetting-timescale (atom 0.2), :latency (atom 0.3)})

"create a structure for dendrites and coordinates"
(defrecord coord [x y z])

"create a structure of records for a citizen"
(defrecord skeleton  [state dendrites coordinate] :load-ns true)

(def neural-cluster (atom 0))

(defn get-atom-x []
  "Returns an x coordinate for the neuron"
  (atom (rand 50)))
(defn get-atom-y []
  "Returns a y coordinate given x coordinate for the neuron"
  (atom (rand 50)))
(defn get-atom-z []
  "Returns a z coordinate given x,y coordinate for the neuron"
  (atom (rand 50)))
(defn create-neuron-file [id]
  "create a file with the supplied id"
  (spit (str "neuron-data/" (name id) ".neuron") "Fuck Google for now")
  (log/log (str "Created file " (name id)))
  )
(defn create-neuron [id]
  (when-not (.exists (io/file (str "neuron-data/" id ".neuron"))) (create-neuron-file id))

  "create a neuron with the given id after converting it into base 32. The dendrites need to be "
  (let [neuron (hash-map (keyword id) (skeleton. (atom 0) (atom {})
                                                 (coord. (get-atom-x) (get-atom-y) (get-atom-z))))] neuron))

(defn createNeuralMap [arr]
  "arr is the set of neurons...lets create the shit"
  (let [neural-map (if (= 0 @neural-cluster) (map #(create-neuron %) (set arr)) (map #(when (= nil (find @neural-cluster %)) (create-neuron %)) (set arr) ))] (log/log (str "Neural map created using : " (vec arr))) neural-map))

(defn init [arr]
 (when-not (.exists (io/file "neuron-data")) (.mkdir (io/file "neuron-data")))
  "Initialize the neural structure....yay it works"
  (let [brain (createNeuralMap arr)] brain))

(defn handle-coord[x]
  (coord. (:x x) (:y x) (:z x))
  )
(defn handle-object [x]
  (def tags {'object handle-object,'nelson2.brain.brain.coord handle-coord })
  (atom (:val (get (edn/read-string {:readers tags} (str x)) 2))))
(defn parse-skeleton [x]
  (def tags {'object handle-object,'nelson2.brain.brain.coord handle-coord })
  (skeleton. (:state x) (:dendrites x) (:coordinate x))
  )