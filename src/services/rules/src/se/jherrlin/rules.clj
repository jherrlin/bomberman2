(ns se.jherrlin.rules
  (:require
   [clara.rules :refer
    [defquery defrule defsession fire-rules insert insert!
     insert-all insert-unconditional! query retract! mk-session]]
   [clara.rules.accumulators :as acc]
   [clara.tools.inspect :as inspect]))



(defrule player-moves
  "Player moves"
  [?timestamp <- :timestamp]
  [:player-move [{id :player-id}]
   (= ?id id)]
  =>
  (insert!
   {:rule/type :player-moves
    :data :d}))



(defquery player-moves? [] [?fact <- :player-moves])
(defquery timestamp?    [] [?fact <- :timestamp])



(let [session (-> (mk-session [player-moves
                               player-moves?
                               timestamp?] :fact-type-fn :rule/type)
                  (insert {:rule/type :timestamp
                           :timestamp (java.util.Date.)}
                          {:rule/type :player-move
                           :id        1})
                  (fire-rules))]
  (->> [player-moves? timestamp?]
       (map (fn [q] (->> (query session q)
                         (map :?fact))))
       (apply concat)
       ))
