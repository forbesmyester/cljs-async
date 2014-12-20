(ns cljs-nodejs-coreasync-bootstrap.hello
  (:require [cljs.core.async :as async :refer [put! <!! >!! <! >! chan close! timeout]]
            [cljs.nodejs :as nodejs])
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]]))

(def che (nodejs/require "./asyncThing.js"))

; (defn <<< [f & args]
;   (let [c (chan)]
;     (apply f (concat args [(fn [x]
;                              (print (str "CB: " x))
;                              (if (or (nil? x)
;                                      (undefined? x))
;                                    (close! c)
;                                    (put! c x)) c)]))))


(defn <<< [f & args]
  (let [c (chan)
        callback (fn [x]
                   (print (str "CB: " x))
                   (if (or (nil? x)
                           (undefined? x))
                     (close! c)
                     (put! c x)))]
    (apply f (concat args [callback])) c))

(defn getCheese [x]
  (go (<! (<<< che 5))))

(defn -main [& args]
  ; (print (<!! (<getCheese 5))))
  ; (print (getCheese 5)))
  (go (print (<! (<<< che 5)))))
  ; (print (<!! getCheese)))

; (let [c (chan 10)]
;   (>!! c "hello")
;   (print (<!! c))
;   (close! c)))

(set! *main-cli-fn* -main)
