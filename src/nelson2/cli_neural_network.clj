(ns nelson2.cli_neural_network
  (:require [nelson2.brain :as brain]
            [nelson2.neural_processes :as neural-processes]
            [nelson2.extract-data-features :as extract-data-features]
            [nelson2.cli_logic :as logic-cli]
            [nelson2.utility :as utility]
            [nelson2.cli_reward :as reward-cli]
            [nelson2.employee-handling :as handler]
            [nelson2.log :as log]
            [nelson2.neural-encoder :as neural-encoder]
            [nelson2.text-input-interface :as text-interface]) (:use [clojure.string :only (index-of)])  (:gen-class))

(defn neural-interaction [text]

    "converting string into neural map"
  (text-interface/parse-text text)
  "log it in the file"
  (log/log (str "New neural map created for string: " text))
  )
(defn neural-command [command]
  (cond
    (= command "@read-file") (do (print "Enter the path of the file : ") (flush) (text-interface/parse-file
                                                                                   (read-line)))
    (= command "@cli-logic") (logic-cli/introduce-logic)
    (= command "@cli-reward") (do (print (str (char 27) "[2J")) (reward-cli/introduce))
    (= command "@generate-personality") (pcalls handler/reward-cluster-engineer)
    (= command "@live-neurons") (println (keys (neural-processes/get-active-neurons)))
    (= command "@forget") (do (print "Enter parent neuron ID ~~ ") (flush) (let [parent (read-line)] (do (print "Enter child neuron ID ~~ ")
                                                                                 (flush) (neural-processes/forget-weight parent (read-line)))) (println "connection erased"))
    (= command "@dump-all") (utility/save-neurons @brain/neural-cluster)
    (= command "@load-all") (neural-processes/load-neurons)
    (= command "@extract-concept") (extract-data-features/get-structure (neural-processes/select-random-tuple) (neural-processes/get-focus))
    (= command "@weight") (println "You are never alone...loneliness will never leave your side")
    (= command "@excite") (do (print "Enter the neuron ID (without :) :- ") (flush) (neural-processes/flush-neuron (read-line)))
    (= command "@summary") (do (println (str "Concept cap           : " @(get brain/params :concept-cap)))
                               (println (str "Time step size        : " @(get brain/params :time-interval)))
                               (println (str "Learning timescale    : " @(get brain/params :learning-timescale)))
                               (println (str "Forgetting timescale  : " @(get brain/params :forgetting-timescale)))
                               (println (str "Latency               : " @(get brain/params :latency)))
                               )
    (= command "@clear") (print (str (char 27) "[2J"))
    (= command "@reinit") (do (brain/reinit) (println "Brain reinitialized"))
    (= command "@flush-neuron") (do (print "Enter neuron-id (without :) :- ")(flush) (println "Neuron flushed with probability :- "(neural-processes/flush-neuron (keyword (read-line)))) )
    (= command "@brain") (println (str "brain = " @brain/neural-cluster))
    (= command "@count-threads") (println (str "The number of threads running are :- " (Thread/activeCount)))
    (= command "@recruit") (handler/recruit)
    (= command "@neural-list") (println (str "Neurons :- " (keys @brain/neural-cluster)))
    (= command "@get-neuron-priority") (do (print "Enter neuron-id (without :) :- ")(flush) (println "priority = " (neural-processes/get-neuron-priority (keyword (read-line)))))
    (= command "@recruit-automatically") (pcalls handler/background-recruit)
    (= command "@activate-neuron") (do (print "Enter the neuron ID (without :) :- ") (flush) (neural-processes/activate-neurons [(keyword (read-line))]))
    (= command "@deactivate-neuron") (do (print "Enter the neuron ID (without :) :- ") (flush) (neural-processes/deactivate-neuron (keyword (read-line))))
    (= command "@init") (do (swap! brain/neural-cluster (fn [_] (into {} (brain/init (range @(get brain/params :gen-pop)))))) (println "Neuron cluster created."))
    (= command "@quit") (do (shutdown-agents)(log/log "All agents shutdown"))
    ;;(= command "@set focus") (println "Focus ou little piece of shit")
    (= command "@set-concept-cap") (do (print "Enter the cap ~~ ") (flush) (swap! (get brain/params :concept-cap) (fn [_] (let [val (Integer/parseInt (read-line))] val))) (log/log (str "Updated params : " brain/params)))
    ;;(= command "@mode=") (println "Dont know...ran out of ideas")
    (= command "@set-latency") (do (print "Enter the latency ~~ ") (flush) (swap! (get brain/params :latency) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@set-timestep") (do (print "Enter the time step size ~~ ") (flush) (swap! (get brain/params :time-interval) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@set-learning-time-scale") (do (print "Enter the learning time scale ~~ ") (flush) (swap! (get brain/params :learning-timescale) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@set-forget-timescale") (do (print "Enter the forgetting timescale ~~ ") (flush) (swap! (get brain/params :forgetting-timescale) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@help") (do
                          (println "@live-neuron            : shows the ids of live neurons")
                          (println "@forget                 : forget all weights of all the neurons")
                          (println "@dump-all               : write the neurons to the corresponding files")
                          (println "@load-all               : load all the neurons from directory")
                          (println "@weight                 : shows the weight between inputted neurons")
                          (println "@excite-prob            : shows the probability of exciting a neuron")
                          (println "@update                 : updates the relationship between inputted neurons")
                          (println "@summary                : shows the current values of the parameters")
                          (println "@clear                  : clears the screen")
                          (println "@kill-all               : de-excites all neurons")
                          (println "@init                   : initialize the brain")
                          (println "@quit                   : quit the CLI")
                          (println "@excite                 : excite a set of neurons")
                          (println "@set focus              : set the focus of the code")
                          (println "@set pop                : set the population of the brain")
                          (println "@set latency            : set the latency of neural processes")
                          (println "@set time-step          : set the time step for the temporal gradients")
                          (println "@set learning timescale : set the timescale for learning")
                          (println "@set forget time scale  : set the timescale for forgetting")
                          )


    :else (println "Off you fuck...")
    )
  )

(defn execute-desire [wish]
  "Execute the wish of the user"
  (if (not= 0 (index-of wish "@" )) (neural-interaction wish) (neural-command wish)))

(defn terminal []
  (do (println "enter '@help' for a glossary of commands") (print "neural-network >> ") (flush))
  (let [input (read-line)]  (execute-desire  input) (when-not (= input "@quit") (recur))))


(defn -main []
  (println "Fuck Google")
  (terminal)
  ;;(create-brain/init 3)
  )
