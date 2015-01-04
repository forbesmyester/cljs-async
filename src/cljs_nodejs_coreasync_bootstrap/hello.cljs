(ns cljs-nodejs-coreasync-bootstrap.hello
  (:require
    [cljs.nodejs :as nodejs]
    [cljs-nodejs-coreasync-bootstrap.async :as callback-async]))

(def node-like-cb-func (nodejs/require "./asyncThing.js"))

; This code is for easier jvm based clojure repl development...
; (defn- node-like-cb-func [in cb]
;   (if (>= 10 in)
;     (cb 0 (+ in 1))
;     (cb 1)))
;
; (defn- async-tester [func & params]
;   (let [a (atom nil)
;         cb (fn [ & xs]
;              (if (= 0 (first xs))
;                (reset! a xs)
;                (reset! a [(first xs)])))
;         ]
;     (apply
;       func
;       (concat params [cb]))
;     @a
;     ))

(defn test-is-equal [expected async-func-missing-callback message]
  (async-func-missing-callback (fn [err result]
                                 (print (str
                                          message "\n\t"
                                          expected "\n\t"
                                          [err result] "\n\n\n")))))


(defn -main [& args]

  ; Map tests
  (test-is-equal [0 [2 3 4]]
                 (partial callback-async/async-map node-like-cb-func [1 2 3])
                 "Standard map")

  (test-is-equal [0 [2 3 11]]
                 (partial callback-async/async-map node-like-cb-func [1 2 10])
                 "Edge case map")

  (test-is-equal [1 nil]
                 (partial callback-async/async-map node-like-cb-func [1 2 11])
                 "Node Err with map")

  (test-is-equal [0 [2 3 4]]
                 (partial callback-async/async-parallel
                          [(partial node-like-cb-func 1) (partial node-like-cb-func 2) (partial node-like-cb-func 3)]
                          )
                 "Standard parallel")
  (test-is-equal [1 nil]
                 (partial callback-async/async-parallel
                          [(partial node-like-cb-func 1) (partial node-like-cb-func 2) (partial node-like-cb-func 11)]
                          )
                 "Err parallel")
  )

(set! *main-cli-fn* -main)
