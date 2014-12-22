(ns cljs-nodejs-coreasync-bootstrap.hello
  (:require [cljs.core.async :as async]
            [cljs.nodejs :as nodejs])
  (:require-macros [cljs.core.async.macros :as asyncm]))

; (require '[clojure.core.async :as async])

(def che (nodejs/require "./asyncThing.js"))

(defn <<<e [f valid-errs & args]
  (let [c (async/chan)
        callback (fn [err x]
                   (if (contains? valid-errs err)
                     (async/put! c x)
                     (async/close! c)))]
    (apply f (concat args [callback])) c))

(defn <<< [f & args] (apply <<<e (concat [f #{0}] args)))

(defn async-map [pred args next-callback]
  (let [res (atom (vec (repeat (count args) nil)))
        nodejs-callback (fn [pos err result]
                          (swap! res assoc pos result)
                          (if (empty? (filter nil? @res))
                            (next-callback 0 @res)
                            ))
        nodejs-fire (fn [pos nodejs-async-func v]
                          (nodejs-async-func v (partial nodejs-callback pos)))
        get-nodejs-fire (fn [pos nodejs-async-func]
                            (partial nodejs-fire pos nodejs-async-func))
        ]
    (loop [f (first args) r (rest args) pos 0]
      ((get-nodejs-fire pos pred)  f)
      (if (= 0 (count r)) 
        nil
        (recur (first r) (rest r) (inc pos))))))

; (let [node-like-cb-func (fn [in cb] (cb 0 (+ in 1)))]
;   (async/go (print (async/<! (<<< async-map node-like-cb-func [1 2 3]))))
;   )

(defn -main [& args]
  (asyncm/go (print (async/<! (<<<e async-map #{0} che [1 2 3]))))
  )

(set! *main-cli-fn* -main)
