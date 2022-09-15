(ns nelson_clojure.cli_neural_network
  (:require [nelson_clojure.brain :as create-brain ] [ nelson_clojure.neural_processes :as neural-processes] [nelson_clojure.extract-data-features :as extract-data-features]
            [nelson_clojure.cli_logic :as logic-cli] [nelson_clojure.utility :as utility] [nelson_clojure.cli_reward :as reward-cli] [nelson_clojure.employee-handling :as handler] [nelson_clojure.log :as log] [nelson_clojure.neural-encoder :as neural-encoder] [nelson_clojure.text-input-interface :as text-interface]) (:use [clojure.string :only (index-of)])  (:gen-class))

(defn neural-interaction [text]
  (let [temp-labels (neural-encoder/get-labels text)]
    (println (str "Text entered is " "and the labels are "))

    )
  )
(defn neural-command [command]
  (cond
    (= command "@read-file") (do (print "Enter the path of the file : ") (flush) (text-interface/parse-file (read-line)))
    (= command "@cli-logic") (logic-cli/introduce-logic)
    (= command "@cli-reward") (do (print (str (char 27) "[2J")) (reward-cli/introduce))
    (= command "@generate-personality") (pcalls handler/reward-cluster-engineer)
    (= command "@live-neurons") (println (keys (neural-processes/get-active-neurons)))
    (= command "@forget") (do (print "Enter parent neuron ID ~~ ") (flush) (let [parent (read-line)] (do (print "Enter child neuron ID ~~ ") (flush) (neural-processes/forget-weight parent (read-line)))) (println "connection erased"))
    (= command "@dump-all") (utility/save-neurons @create-brain/neural-cluster)
    (= command "@load-all") (neural-processes/load-neurons)
    (= command "@extract-concept") (extract-data-features/get-structure (neural-processes/select-random-tuple) (neural-processes/get-focus))
    (= command "@weight") (println "You are never alone...loneliness will never leave your side")
    (= command "@excite") (do (print "Enter the neuron ID (without :) :- ") (flush) (neural-processes/flush-neuron (read-line)))
    (= command "@summary") (do (println (str "Concept cap           : " @(get create-brain/params :concept-cap)))
                               (println (str "Time step size        : " @(get create-brain/params :time-interval)))
                               (println (str "Learning timescale    : " @(get create-brain/params :learning-timescale)))
                               (println (str "Forgetting timescale  : " @(get create-brain/params :forgetting-timescale)))
                               (println (str "Latency               : " @(get create-brain/params :latency)))
                               )
    (= command "@clear") (print (str (char 27) "[2J"))
    (= command "@recruit") (handler/recruit)
    (= command "@recruit-automatically") (pcalls handler/background-recruit)
    (= command "@activate-neuron") (do (print "Enter the neuron ID (without :) :- ") (flush) (neural-processes/activate-neurons [(keyword (read-line))]))
    (= command "@deactivate-neuron") (do (print "Enter the neuron ID (without :) :- ") (flush) (neural-processes/deactivate-neuron (read-line)))
    (= command "@init") (do (swap! create-brain/neural-cluster (fn [_] (into {} (create-brain/init (range @(get create-brain/params :gen-pop)))))) (println "Neuron cluster created."))
    (= command "@quit") (do (shutdown-agents)(log/log "All agents shutdown"))
    ;;(= command "@set focus") (println "Focus ou little piece of shit")
    (= command "@set-concept-cap") (do (print "Enter the cap ~~ ") (flush) (swap! (get create-brain/params :concept-cap) (fn [_] (let [val (Integer/parseInt (read-line))] val))) (log/log (str "Updated params : " create-brain/params)))
    ;;(= command "@mode=") (println "Dont know...ran out of ideas")
    (= command "@set-latency") (do (print "Enter the latency ~~ ") (flush) (swap! (get create-brain/params :latency) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@set-timestep") (do (print "Enter the time step size ~~ ") (flush) (swap! (get create-brain/params :time-interval) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@set-learning-time-scale") (do (print "Enter the learning time scale ~~ ") (flush) (swap! (get create-brain/params :learning-timescale) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
    (= command "@set-forget-timescale") (do (print "Enter the forgetting timescale ~~ ") (flush) (swap! (get create-brain/params :forgetting-timescale) (fn [_] (let [val (Double/parseDouble (read-line))] val))))
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
