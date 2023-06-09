(ns nelson2.weight_update
  (:gen-class))

;;(refer ('cli :only params))

(defn forget-update [weight params]
  (let [parameters params] (- weight (Math/exp (+ (/ @(get parameters :time-interval) @(get parameters :forgetting-timescale)))))))

(defn forget [weights child-key params]
  (swap! (get weights child-key) (fn [_] (forget-update @(get weights child-key) params))))

(defn learn-update [weight params]
  (let [parameters params] (+ weight (Math/exp (/ @(get parameters :time-interval) @(get parameters :learning-timescale))))))

(defn learn [weights child-key params]
  (swap! (get weights child-key) (fn [_] (learn-update @(get weights child-key) params))))