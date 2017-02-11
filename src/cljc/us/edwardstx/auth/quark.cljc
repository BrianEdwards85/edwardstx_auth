(ns us.edwardstx.auth.quark)

(defn quark
  ([a ks] (quark a ks atom))
  ( [a ks _atom]
   (let [sa (_atom (get-in @a ks))]
     (do
       (add-watch a ks
                  (fn [k ax o n]
                    (when (not= (get-in o ks) (get-in n ks))
                      (reset! sa (get-in n ks) ))))
       (add-watch sa ks
                  (fn [k ax o n]
                    (when (not= o n )
                      (swap! a #(assoc-in % ks n)))))))))



(defn reset-in!
  ([atom ks newval]
   (if (empty? ks)
     (reset! atom newval)
     (swap! atom #(assoc-in % ks newval))))
  ([atom ks]
   (if (empty? ks)
     #(reset! atom %)
     (fn [newval]
       (swap! atom #(assoc-in % ks newval))))))

(defn swap-in!
  ([atom ks f]
   (if (empty? ks)
     (swap! atom f)
     (swap! atom
            (fn [a]
              (let [v (get-in a ks)
                    newval (f v)]
                (assoc-in a ks newval))))))
  ([atom ks]
   (if (empty? ks)
     #(swap! atom %)
     (fn [f]
       (swap! atom
              (fn [a]
                (let [v (get-in a ks)
                      newval (f v)]
                  (assoc-in a ks newval))))))))

