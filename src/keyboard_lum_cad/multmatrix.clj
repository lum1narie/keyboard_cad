(ns keyboard-lum-cad.multmatrix
  (:refer-clojure :exclude [use import + - * / == < <= > >= not= = min max])
  (:require [scad-clj.scad :as scad]
            [clojure.core.matrix :as mx]
            [clojure.core.matrix.operators :refer :all]))

(mx/set-current-implementation :vectorz)

(defmulti multmatrix
  "get scad model which shows multmatri"
  (fn [m & block] (type m)))
(defmethod multmatrix clojure.lang.PersistentVector [m & block]
  (let [m3-4 (mx/select m (range 3) (range 4))]
    `(:multmatrix ~m3-4 ~@block)))
(defmethod multmatrix :default [m & block]
  (let [m3-4 (mx/select m (range 3) (range 4))]
    `(:multmatrix ~m3-4 ~@block)))

(defmethod scad/write-expr :multmatrix [depth [form m & block]]
  (let [w (fn [s] (str "[" s "]")) ;; wrap
        co (fn [c] (apply str (interpose "," c)))] ;; put commas in between
    (concat
     (list (scad/indent depth) "multmatrix(")
     (w (co (map #(w (co %)) m)))
     (list ") {\n")
     (mapcat #(scad/write-expr (inc depth) %1) block)
     (list (scad/indent depth) "}\n"))))

(defn trans-matrix
  "get translate affine transformation matrix"
  [x y z]
  (mx/matrix [[1 0 0 x]
              [0 1 0 y]
              [0 0 1 z]
              [0 0 0 1]]))

(defn rotv-matrix
  "get rotate affine transformation matrix
  this will rotate around given vector"
  [a [x y z]]
  (mx/matrix [[(+ (Math/cos a) (* (Math/pow x 2) (- 1 (Math/cos a))))
               (- (* x y (- 1 (Math/cos a))) (* z (Math/sin a)))
               (+ (* x z (- 1 (Math/cos a))) (* y (Math/sin a)))
               0]
              [(+ (* x y (- 1 (Math/cos a))) (* z (Math/sin a)))
               (+ (Math/cos a) (* (Math/pow y 2) (- 1 (Math/cos a))))
               (- (* y z (- 1 (Math/cos a))) (* x (Math/sin a)))
               0]
              [(- (* x z (- 1 (Math/cos a))) (* y (Math/sin a)))
               (+ (* y z (- 1 (Math/cos a))) (* x (Math/sin a)))
               (+ (Math/cos a) (* (Math/pow z 2) (- 1 (Math/cos a)))) 0]
              [0 0 0 1]]))

(defn rotc-matrix
  "get rotate affine transformation matrix
  this will rotate around axes"
  [[x y z]]
  (mx/inner-product (rotv-matrix x [1 0 0])
                    (rotv-matrix y [0 1 0])
                    (rotv-matrix z [0 0 1])))

(defn rot-matrix
  "get rotate affine transformation matrix"
  [& args]
  (if (number? (first args))
    (rotv-matrix (first args) (rest args))
    (rotc-matrix (first args))))

(defn scale-matrix
  "get scale affine transformation matrix"
  [x y z]
  (mx/matrix [[x 0 0 0]
              [0 y 0 0]
              [0 0 z 0]
              [0 0 0 1]]))

(defn to-3-3-matrix
  "get the intersections of top 3 rows and left 3 columns to make 3x3 matrix
  from given matrix"
  [m]
  (mx/select m (range 3) (range 3)))
