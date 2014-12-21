(ns cljs-nodejs-coreasync-bootstrap.hello
  (:require [cljs.core.async :as async]
            [cljs.nodejs :as nodejs])
  (:require-macros [cljs.core.async.macros :as asyncm]))

(def che (nodejs/require "./asyncThing.js"))

(defn <<< [f & args]
  (let [c (async/chan)
        callback (fn [x]
                   (if (or (nil? x)
                           (undefined? x))
                     (async/close! c)
                     (async/put! c x)))]
    (apply f (concat args [callback])) c))

; (defn <<< [f & args]
;   (let [c (chan)]
;     (apply f (concat args [(fn [x]
;                              (print (str "CB: " x))
;                              (if (or (nil? x)
;                                      (undefined? x))
;                                    (close! c)
;                                    (put! c x)) c)]))))

; (defn <getCheese [x]
;   (<<< che 5))

; (defn gc []
;   (let [c (chan)]
;     (go (>! c (<! (<<< che 5)))) c))

(defn <async-worker [pred args]
  (let [
        call-if-done (fn [ar done] (if (= 0 (count (filter nil? ar))) (done)) nil)
        return-channel (async/chan)
        res (atom (vec (repeat (count args) nil)))
        get-async-splicer (fn [p nodejs-async-func]
                        (fn [v]
                          (nodejs-async-func
                            v
                            (fn [err result]
                              (swap! res assoc-in [p] result)
                              (call-if-done @res #(async/put! return-channel @res))
                          ))))
        ]
    (loop [f (first args) r (rest args) pos 0]
      ((get-async-splicer pos pred)  f)
      (if (= 0 (count r)) 
        ; (async/go (print (async/<! return-channel)))
        return-channel
        (recur (first r) (rest r) (inc pos))
        )
      )
    )
  )

(let [node-like-cb-func (fn [in cb] (cb 0 (+ in 1)))]
  (async/go (print (async/<! (<async-worker node-like-cb-func [1 2 3]))))
  )

(defn -main [& args]
  ; (let [cs (merge [(<getCheese 5) (<getCheese 5)])]
  ;   (go (print (<! cs)))
  ; (print (<!! (<getCheese 5))))
  ; (print (getCheese 5)))
  ; (go (print (map (fn [x] (str <! x)) [(<getCheese 5) (<getCheese 5)])))
  (asyncm/go (print (async/<! (<async-worker che [1 2 3]))))
  )  ; (print (<!! getCheese)))

; (let [c (chan 10)]
;   (>!! c "hello")
;   (print (<!! c))
;   (close! c)))

(set! *main-cli-fn* -main)
