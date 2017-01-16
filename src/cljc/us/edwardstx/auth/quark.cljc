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




;;(deftype Quark [source-atom ks]
;;  IAtom

;;  IEquiv
;;  (-equiv [o other] (identical? o other))

;;  IDeref
;;  (-deref [this]
;;    (get-in @source-atom ks))

;;  IReset
;;  (-reset! [a new-value]
;;    (swap! source-atom #(assoc-in % ks new-value)))

;;  ISwap
;;  (-swap! [a f]
;;    (swap! source-atom
;;           (fn [x] (let [sub-val (get-in x ks)]
;;                     (assoc-in x ks (f sub-val))))))
;;  (-swap! [a f i]
;;    (swap! source-atom
;;           (fn [x] (let [sub-val (get-in x ks)]
;;                     (assoc-in x ks (f sub-val i))))))
;;  (-swap! [a f i j]
;;    (swap! source-atom
;;           (fn [x] (let [sub-val (get-in x ks)]
;;                     (assoc-in x ks (f sub-val i j))))))
;;  (-swap! [a f i j more]
;;    (swap! source-atom
;;           (fn [x] (let [sub-val (get-in x ks)]
;;                     (assoc-in x ks (apply f sub-val i j more))))))
;;  IMeta
;;  (-meta [_] meta)

;;  IHash
;;  (-hash [this] (+ (hash source-atom) (hash ks)))


;;#?(:cljs IWatchable
;;   (-notify-watches [this old new] (str old new)) ;;(-notify-watches source-atom old new))
;;   (-add-watch      [this key f]   (add-watch source-atom key f))
;;   (-remove-watch   [this key]     (remove-watch source-atom key))
;;)

;;)

;;(defn quark [source-atom ks]
;;  (Quark. source-atom ks))
