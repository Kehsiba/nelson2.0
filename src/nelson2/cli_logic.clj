(ns nelson2.cli_logic
  (:require [nelson2.logic_process :as logic-process])
  (:use [clojure.string :only (index-of)] [clojure.edn :as edn]) (:gen-class))
"CLI for the logic sector"

(defn cluster-command [command]
  (cond
    (= command "@keep-activated") (do (print "Enter the name of neuron that you want activated :- ") (flush)
                                      (logic-process/activate-all [(keyword (read-line))]))
    (= command "@remember-concepts") (do (print "Enter the name of neuron :- ") (flush)
                                          (println "inferred-concepts :- " (logic-process/infer-concept-neurons [(keyword (read-line))])))
    (= command "@start-logic-thread") (do (print "Enter the name of initial neuron :- ") (flush)
                                         (println (str "logic retrieved : " @(logic-process/start-logic-thread
                                                                         (keyword (read-line)))))
                                          )
    (= command "@time-corr") (do (print "Enter time interval (integers only): ") (flush) (logic-process/causally-correlate (Double/parseDouble (read-line))))
    (= command "@expand-logic-thread") (do (print "Enter the logic thread :- ") (flush)
                                           (println  (str "Expanded thread : " (logic-process/expand-logic-thread (mapv #(keyword %) (edn/read-string(read-line)))))))
    (= command "@infer") (do (print "Enter the initial logic thread :- ") (flush)
                             (println  "report : " (logic-process/logical-inference (edn/read-string (read-line)))))
    (= command "@sample-spontaneous") (logic-process/sample-spontaneous-mode)
    (= command "@sample-response") (do ("Enter a sentence :- ") (flush) (logic-process/sample-response-mode (read-line)))
    (= command "@help") (do (println "rrthui"))
    )
  )

(defn execute-desire [wish]
  "Execute the wish of the user"
  (when (= 0 (index-of wish "@" )) (cluster-command wish)))

(defn terminal []
  (do (println "enter '@help' for a glossary of commands") (print "logic >> ") (flush))
  (let [input (read-line)]  (execute-desire  input) (when-not (= input "@quit") (recur))))

(defn introduce-logic []
  (println "Fuck Google")
  (println "^^^^^^^^^^^^^^^^^^This is a terminal for probing the logical inference^^^^^^^^^^^^^^^^^^^^") (terminal))