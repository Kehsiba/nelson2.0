(ns nelson2.text_output_interface (:require
                                    [nelson2.log :as log] [clojure.edn :as edn]
                                    [nelson2.utility :as utility]
                                    [nelson2.logic_process :as logic-process]
                                    ))

"read the file"
(def raw-data)
(defn read-file [path]
  "Read the file specified by the path"
  (let [f (java.io.File. path)
        ary (byte-array (.length f))
        ins (java.io.FileInputStream. f)]
    (.read ins ary)
    (.close ins) ary))

(defn get-raw-data [path]
  (slurp  path)
  )
(defn handle-object [x]
  (def tags {'object handle-object})
  (atom (:val (get (edn/read-string (str x)) 2))))

(defn parse-file-data [path]
  "load neurons from file"
  ;;(def tags {'object handle-object})
  (let [x (edn/read-string (get-raw-data path))] x)
  )

(defn parse-data-from-file [path]
  (if (= "" path)
    (parse-file-data "neural_network_output.txt")
    (parse-file-data path)
    )

  )
(defn process-to-characters [list]
  "take the list and print the character"
  (println "**" (clojure.string/join (map #(char (utility/base32todec (name %))) list)) "**")
  )
(defn parse-line [line]
  (process-to-characters (logic-process/expand-logic-thread (:logic-thread line)))
  )

"parse the output"
(defn parse-output [path]
  (doseq [line (parse-data-from-file path) sub-line line]
    (parse-line sub-line)
    )
  )


