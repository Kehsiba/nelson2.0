(ns nelson_clojure.text-input-interface
  (:require [nelson_clojure.neural-encoder :as encoder]
            [nelson_clojure.brain :as brain]
            [nelson_clojure.log :as log]
            [nelson_clojure.neural_processes :as neural-processes]))

(defn read-file [path]
"Read the file specified by the path"
  (let [f (java.io.File. path)
        ary (byte-array (.length f))
        ins (java.io.FileInputStream. f)]
    (.read ins ary)
    (.close ins) ary))

(defn create-new-neurons [id]
  "check if the neurons exist in the neural cluster"
  (let [new-neurons (filter (fn [x] (if (= nil (get @brain/neural-cluster x)) true false)) id)] (log/log (str "New neurons :- " (into [] new-neurons)))
                                                                         (swap! brain/neural-cluster (fn [_] (merge @brain/neural-cluster (into {} (brain/init id))))))
  )

(defn parse-file [path]
  (log/log (str "Reading file :- " path))
  (let [data-labels (encoder/get-labels (read-file path))]
    (if (= 0 @brain/neural-cluster) (swap! brain/neural-cluster (fn [_] (into {} (brain/init (set data-labels)))))
                                    (create-new-neurons data-labels))
    (neural-processes/create-neural-map data-labels))
  )