(ns stencil.transform
  "Manipulate metas *in the AST*.  
   Functions for working with metas outside the AST (e.g., as a dictionary) are found elsewhere.")

(defn- atom-not-form? [a] (and (atom? a) (not (stencil-form? a))))

(defn supply-metas [program] 
  "Ensure that there is a meta expression after every atom."
  (match program
    (a :guard atom-not-form?) a
    (a :guard atom?) (list a '($meta))
    
    (a :guard empty?) a
    
    ([(a :guard atom?) (b :guard meta?) & tail] :seq) 
       `(~a ~b ~@(supply-metas tail))
    ([(a :guard atom-not-form?) & tail] :seq)
       `(~a (~'$meta) ~@(supply-metas tail))
    ([(a :guard atom?) & tail] :seq)
       `(~a ~@(supply-metas tail))
    ([a & tail] :seq)
       (cons (supply-metas a) (supply-metas tail))))
  
(defn meta-pairings [program]
  (letfn [(maybe-pair [ls]
            (cond
              (or (empty? ls) (< (count ls) 3)) ls
              (= '$C (second ls)) 
                 (cons (list (first ls) (nth ls 2)) (maybe-pair (drop 3 ls)))
              :else (cons (first ls) (maybe-pair (rest ls)))))]
    (match program
      (a :guard atom?) a
      (['$meta & rest] :seq) (cons '$meta (maybe-pair rest))
      :else (map meta-pairings program))))


(defn meta-types
  "Identify data types in meta statements.
  TODO: REMOVE THIS AND REPLACE WITH A MORE COMPLETE METADATA LABELING MECHANISM."
  [program]
  (letfn 
    [(hasType? [metas] (and (seq? metas) (contains? (meta->map metas) 'type)))
     (addType [metas] (let [[before after] (split-with seq? (rest metas))
                            [type & tail] after]
                        (if (nil? type) 
                          metas
                          (cons '$meta (concat before (cons (list 'type type) tail))))))]
    (match program
      (a :guard [meta? hasType?]) a
      (a :guard meta?) (addType a)
      (a :guard atom?) a
      :else (map meta-types program))))


(defn clean-metas
  "Remove empty metas...mostly for pretty-printing"
  [program]
  (if (seq? program)
    (map clean-metas (remove empty-meta? program))
    program))


(defn tie-metas [program]
  "Put a meta and its associated item in to a list together.  
   This is a utility for working with metas when the item is also required."
  (match program
    (a :guard atom?) a
    (m :guard meta?) m
    (b :guard empty?) nil 
    ([(a :guard atom?) (m :guard meta?) & rest] :seq)
       `((~a ~m) ~@(tie-metas rest))
    ([e & rest] :seq)
      `(~(tie-metas e) ~@(tie-metas rest))))


(defn untie-metas [program]
  "Undoes tie-metas."
  (match program
    (a :guard atom?) a
    (n :guard empty?) n
    ([([(a :guard atom?) (m :guard meta?)] :seq) & rest] :seq)
      `(~a ~m ~@(untie-metas rest))
    ([e & rest] :seq)
      `(~(untie-metas e) ~@(untie-metas rest))))
