(ns nelson2.reward-log
  (:require [clojure.java.io :as io])
  (:import (java.util Date)))
(defn timestamp []
(Math/log (.getTime (Date.)))
  )
(defn log [msg]
  (when-not (.exists (io/file "log-data")) (.mkdir (io/file "log-data")))
  (spit "log-data/log-personality.txt" (str (timestamp) ": " msg "\n") :append true)
  )