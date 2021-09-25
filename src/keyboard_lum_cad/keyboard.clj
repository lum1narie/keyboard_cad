(ns keyboard-lum-cad.keyboard
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :as model]
            [keyboard-lum-cad.mugen :as mugen]
            [keyboard-lum-cad.mount-hole :as mhole]
            [keyboard-lum-cad.keycap-mock :as kmock]))

(def mount-foot-distance 6)

(defn translate-key
  "translate block to key's position"
  [row col block]
  (let [r 90

        row-angle (/ model/pi 12)
        col-angle (/ model/pi 12)

        theta (* (- 5 col) row-angle)
        omega (* (- row 2) col-angle)]

    (->> block
         (model/translate [0 0 (- r)])
         (model/rotate [(- omega) 0 0])
         (model/rotate [0 theta 0])
         (model/translate [0 0 r]))))

(defn translate-mugen
  "translate block to mugen's position"
  [row col block]
  (->> block
       (model/translate [0 0 (- mount-foot-distance)])
       (translate-key row col)))

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
