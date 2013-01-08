(in-ns 'stencil.transform)


(defn UNKNOWN  [] '***)
(defn context? [l] (or (= l 'stencil) (= l 'stream) (= l 'table) (= l 'import)))
(defn policy? [p] (or (= p 'fields) (= p 'data) (= p 'render)))


(defn simple-type [a]
  (if (symbol? a) 
    (UNKNOWN)
    (type a)))


(defn ensure-type [meta type]
  "Add the given type, if there is not already a type present"
  (let [meta (meta->map meta)]
    (map->meta 
      (if (nil? (meta 'type))
        (assoc meta 'type type)
        meta))))

(defn enforce-type 
  "Set the type to the passed one.  
   Throw an error if there is another type present UNLESS the one present returns true from replace?"
  ([meta required] (enforce-type meta required  #(false)))
  ([meta required replace?]
   (let [meta (meta->map meta)
         present (meta 'type)]
     (map->meta 
       (cond
         (= present required) meta
         (or (nil? present) (replace? present)) (assoc meta 'type required) 
         :else (throw (RuntimeException. (str "Found type '" present "' when '" required "' was required."))))))))


(defn infer-types [program]
  "A very simple type annotater (not really an inferencer) 
   Types are string right now...unless otherwise state or false from immediate context (non-string primitive, function calls, etc)."
  (letfn 
    [(annotate [phase program]
       "Puts a type statement into each meta, populated with syntactically-determinable types (when possible)"
       (match [phase program]
         [_ (a :guard atom?)] a
         [_ ([(context :guard context?) ([(name :guard symbol?) (m :guard meta?)] :seq) & rest] :seq)]  
            `(~context ~name ~(enforce-type m context) ~@(map (partial annotate :normal) rest))
         [_ ([([(policy :guard policy?) (m :guard meta?)] :seq) & rest] :seq)]
            `((~policy ~(enforce-type m policy)) ~@(map (partial annotate :arg) rest))
         [_ (['let bindings body] :seq)]
            (list 'let (map (partial annotate :binding) bindings) (annotate :normal body))
         [:normal ([([op (m :guard meta?)] :seq) & args] :seq)]
            `((~op ~(enforce-type m 'fn)) ~@(map (partial annotate :arg) args))
         [:arg ([(a :guard atom?) (m :guard meta?)] :seq)]
            (list a (ensure-type m (simple-type a)))
         [:arg _] 
            (annotate :normal program)
         [:binding ([bindings expr] :seq)]
            (let [vars (map first bindings)
                  metas (map ensure-type (map second bindings) (repeat (UNKNOWN)))
                  bindings (map list vars metas)]
              (list bindings (annotate :normal expr)))
         :else (map (partial annotate :normal) program)))
     
     
     (tuple-patch-kludge [program]
       "REMOVE THIS AS SOON AS YOU CAN!!!!
        Annotates known-tuple producers with tuple-producing type information.  This is only here because I don't have a type registery for functions."
       (letfn [(known-type [a m]
                 (if (or (= a '$ptuple)
                         (= a '$tuple))
                   (enforce-type m '(fn (...) (tuple (...))) #(= % 'fn))
                   m))]
         (match [program]
           [(a :guard atom?)] a
           [([f (m :guard meta?)] :seq)] (list f (known-type f m))
           :else (map tuple-patch-kludge program))))]

    (->> program tie-metas (annotate :normal) tuple-patch-kludge untie-metas)))
