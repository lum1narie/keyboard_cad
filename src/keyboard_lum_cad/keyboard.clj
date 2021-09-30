(ns keyboard-lum-cad.keyboard
  (:refer-clojure :exclude [use import + - * / == < <= > >= not= = min max])
  (:require [scad-clj.model :as model]
            [keyboard-lum-cad.mugen :as mugen]
            [keyboard-lum-cad.mount-hole :as mhole]
            [keyboard-lum-cad.keycap-mock :as kmock]
            [keyboard-lum-cad.multmatrix :as mmx]
            [clojure.core.matrix :as mx]
            [clojure.core.matrix.operators :refer :all]))

(mx/set-current-implementation :vectorz)

(def mount-foot-distance 6.0)

(defn translate-mugen
  "translate block to mugen's position"
  [row col block]
  (let [r 70.0
        m (mx/matrix [-80.0 115.0 90.0])

        row-angle (/ model/pi 18)
        row-angle-mf (/ model/pi 18)
        col-angle (/ model/pi 10)
        col-angle-inside (- (/ model/pi 2))
        finger-interval 21.5

        theta (-> (- col 3) (* row-angle) (+ row-angle-mf))
        omega (-> (- 4 row) (* col-angle) (+ col-angle-inside))
        inside-pos (+ m
                      (mx/inner-product
                       (mmx/to-3-3-matrix
                        (mmx/rot-matrix [0 0 (- row-angle-mf)]))
                       (mmx/to-3-3-matrix
                        (mmx/rot-matrix [col-angle-inside 0 0]))
                       (mx/matrix
                        [(-> (- col 3) (* finger-interval))
                         0
                         (-> (- col 3) (Math/pow 2) (* 2) (+ r) (-))])))
        inside-r-vec (mx/inner-product
                      (mmx/to-3-3-matrix
                       (mmx/rot-matrix [0 0 (- theta)]))
                      (mmx/to-3-3-matrix
                       (mmx/rot-matrix [col-angle-inside 0 0]))
                      (mx/matrix [0 0 (- r)]))
        r-vec (mx/inner-product
               (mmx/to-3-3-matrix
                (mmx/rot-matrix [0 0 (- theta)]))
               (mmx/to-3-3-matrix
                (mmx/rot-matrix [omega 0 0]))
               (mx/matrix [0 0 (- r)]))
        key-pos (-> inside-pos
                    (- inside-r-vec)
                    (+ r-vec))

        trans-mat (mx/inner-product
                   (apply mmx/trans-matrix key-pos)
                   (mmx/rot-matrix [0 0 (- theta)])
                   (mmx/rot-matrix [omega 0 0]))]
    (->> block
         (mmx/multmatrix trans-mat))))

(defn translate-key
  "translate block to key's position"
  [row col block]
  (->> block
       (model/translate [0 0 mount-foot-distance])
       (translate-mugen row col)))

(defn place-grid
  "place solid object into grid"
  [row-max col-max trans-func solid]
  (apply model/union
         (for [x (range 1 (+ 1 row-max))
               y (range 1 (+ 1 col-max))]
           (trans-func x y solid))))

(defn fill-grid
  "fill hull within corners in grid"
  [row-max col-max trans-func corners]
  (let [vertical-fill (fn [row col trans-func corners]
                        (model/hull (trans-func row col
                                                (corners :left-down))
                                    (trans-func (+ row 1) col
                                                (corners :left-up))
                                    (trans-func (+ row 1) col
                                                (corners :right-up))
                                    (trans-func row col
                                                (corners :right-down))))
        horizontal-fill (fn [row col trans-func corners]
                          (model/hull (trans-func row col
                                                  (corners :right-up))
                                      (trans-func row col
                                                  (corners :right-down))
                                      (trans-func row (+ col 1)
                                                  (corners :left-down))
                                      (trans-func row (+ col 1)
                                                  (corners :left-up))))
        diagonal-fill (fn [row col trans-func corners]
                        (model/hull (trans-func row (+ col 1)
                                                (corners :left-down))
                                    (trans-func row col
                                                (corners :right-down))
                                    (trans-func (+ row 1) col
                                                (corners :right-up))
                                    (trans-func (+ row 1) (+ col 1)
                                                (corners :left-up))))

        fills-vertical (for [x (range 1 row-max)
                             y (range 1 (+ col-max 1))]
                         (vertical-fill x y trans-func corners))
        fills-horizontal (for [x (range 1 (+ row-max 1))
                               y (range 1 col-max)]
                           (horizontal-fill x y trans-func corners))
        fills-diagonal (for [x (range 1 row-max)
                             y (range 1 col-max)]
                         (diagonal-fill x y trans-func corners))]

    (apply model/union
           (concat fills-vertical
                   fills-horizontal
                   fills-diagonal))))

(defn place-and-fill-grid
  "place solid and fill hull within corners as grid"
  [row-max col-max trans-func solid corners]
  (model/union (place-grid row-max col-max trans-func solid)
               (fill-grid row-max col-max trans-func corners)))

(def keyboard
  "WIP keyboard object"
  (let [row-num 3
        col-num 5

        mount (place-and-fill-grid row-num col-num translate-key
                                   mhole/mount-hole mhole/mount-corners)

        mugen (place-and-fill-grid row-num col-num translate-mugen
                                   mugen/mugen-cover mugen/mugen-corners)]
    (model/union mount mugen)))

(defn translate-keycap
  "translate block to keycap's position"
  [row col block]
  (->> block
       (model/translate [0 0 2.0])
       (translate-key row col)))

(def keyboard-test
  "test object for keyboard, includes mock keycaps"
  (let [row-num 3
        col-num 5

        keycaps (place-grid row-num col-num translate-keycap
                            kmock/keycap-mock)
        keyboard-with-cap (model/union keyboard keycaps)]
    keyboard-with-cap))
