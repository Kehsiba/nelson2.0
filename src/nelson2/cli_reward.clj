(ns nelson2.cli_reward (:require
                         [nelson2.reward-cluster :as cluster]
                         [nelson2.employee-handling :as handler]
                         [nelson2.reward-moderator :as moderator]
                         [nelson2.neural_processes :as neural_processes])
  (:use [clojure.string :only (index-of)])  (:gen-class))
"The cli for building and probing personalities"


(defn cluster-command [command]
  "Interact with the reward cluster"
  (cond
       (= command "@live-neurons") (println (cluster/get-live-neurons))
       (= command "@dump-all") (cluster/save-neurons @cluster/personality)
       (= command "@count-threads") (println (str "The number of threads running are :- " (Thread/activeCount)))
       (= command "@load-all") (cluster/load-reward-neurons)
       (= command "@maximize-reward") (do (print "Enter max iteration number : ") (flush) (moderator/maximize-reward (Integer/parseInt (read-line))))
       (= command "@create-neurons") (do (print "Enter the names of the neurons : ") (flush) (cluster/create-neuron (read-line)))
       (= command "@create-cluster" )(cluster/create-reward-cluster)
       (= command "@activate-neuron") (do (print "Enter the name of the neuron : ") (flush) (cluster/activate-neuron (keyword (read-line))))
       (= command "@deactivate-neuron") (do (print "Enter the name of the neuron : ") (flush) (cluster/deactivate-neuron (keyword (read-line))))
       (= command "@mutate-personality" ) (cluster/mutate-cluster)
       (= command "@neural-list") (println (str "Neurons :- " (keys @cluster/personality)))
       (= command "@flush-neuron") (do (print "Enter the name of the neuron : ") (flush) (cluster/flush (keyword (read-line))))
       (= command "@quit") (println "Hope to see you soon...")
       (= command "@recruit-automatically") (handler/automatically-manage-reward-center)
       (= command "@connect-brain") (cluster/connect-to-brain)
       (= command "@calculate-interest") (do (print "Enter the name of the neuron :") (flush) (cluster/calc-interest-of-neuron (read-line)))
       (= command "@create-connect-reward-center") (cluster/create-connect-reward-center)
       (= command "@calc-reward") (println "Score = " (moderator/calc-reward))
       (= command "@help")(do
                            (println "Ill help you with everything you need.")
                            (println " ")
                            (println "@create-neurons     : To create new whores")
                            (println "@live-neurons       : to check which whores are available for working")
                            (println "@create-cluster     : To create an orgy of whores.")
                            (println "@mutate-personality : You know what it means...")
                            (println "@quit               : Take you someplace else....")
                            (println " ")
                            )
       :else (println "Its not your fault baby..just try better next time.")


       )
  )
(defn execute-desire [wish]
  "Execute the wish of the user"
  (when (= 0 (index-of wish "@" )) (cluster-command wish)))

(defn terminal []
  (do (println "enter '@help' for a glossary of commands") (print "reward >> ") (flush))
  (let [input (read-line)]  (execute-desire  input) (when-not (= input "@quit") (recur))))

(defn introduce []
  (println "Fuck Google")
  (println "^^^^^^^^^^^^^^^^^^This is a terminal for probing the reward cluster^^^^^^^^^^^^^^^^^^^^") (terminal))