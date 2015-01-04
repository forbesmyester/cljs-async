(ns cljs-nodejs-coreasync-bootstrap.async)


(defn- map-many-results-to-fire-next-callbacks [next-callbacks results]
  (let [all-errs (map first results)
        real-errs (filter #(not (= 0 %)) all-errs)
        err-caller #(% (first real-errs) nil)
        success-caller #(% (first all-errs) (vec (map second results)))
        caller (if (empty? real-errs) success-caller err-caller)
        ]
    (loop [fst (first next-callbacks) rst (rest next-callbacks)]
      (caller fst)
      (if (= 0 (count rst))
        nil
        (recur (first rst) (rest rst))))))

(defn ^:export async-parallel [inputs output]
  (let [res (atom (vec (repeat (count inputs) nil))) ; Atom to collect results.
        nodejs-callback (fn [pos err result] ; Will put the callback result in the correct place in
                          ; the `res` atom then if the `res` atom has no nils
                          ; left in it will call the
                          ; `map-many-results-to-fire-next-callbacks` which will
                          ; despatch the final answer to output.
                          (swap! res assoc pos [err result])
                          (if (empty? (filter nil? @res))
                            (map-many-results-to-fire-next-callbacks [output] @res)
                            ))
        nodejs-fire (fn [pos] ; Fires a function in `inputs` (denoted by `pos`) registering the
                      ; `nodejs-callback` function to collect the result (in the right spot)
                      ((inputs pos) (partial nodejs-callback pos)))
        ]
    (loop [f (first inputs) r (rest inputs) pos 0] ; Loop around the inputs and fire the callbacks
      (nodejs-fire pos)
      (if (= 0 (count r))
        nil
        (recur (first r) (rest r) (inc pos))))))

(defn ^:export async-map [pred args next-callback]
  (let [inputs (vec (map #(partial pred %) args))
        cb (fn [ e xs ]
             (if (= 0 e)
               (next-callback e xs)
               (next-callback e nil)))
        ]
    (async-parallel inputs cb)))


