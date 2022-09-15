(ns nelson2.text-input-interface
  (:require [nelson2.neural-encoder :as encoder]
            [nelson2.brain :as brain]
            [nelson2.log :as log]
            [nelson2.neural_processes :as neural-processes]))

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
(defn parse-text [text]
  "Take a string and create a neural map"
  (let [string-byte (bytes (byte-array (map (comp byte int) text)))
        data-labels (encoder/get-labels string-byte)]
    "Creating the neural map"
    (if (= 0 @brain/neural-cluster) (swap! brain/neural-cluster (fn [_] (into {} (brain/init (set data-labels)))))
                                    (create-new-neurons data-labels))
    (neural-processes/create-neural-map data-labels)
    (log/log "Neural map created")
    )
  )

(defn parse-file [path]
  (log/log (str "Reading file :- " path))
  (let [data-labels (encoder/get-labels (read-file path))]
    (if (= 0 @brain/neural-cluster) (swap! brain/neural-cluster (fn [_] (into {} (brain/init (set data-labels)))))
                                    (create-new-neurons data-labels))
    (neural-processes/create-neural-map data-labels))
  )