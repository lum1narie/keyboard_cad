(ns keyboard-lum-cad.mount-hole
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

;; define variables
(def hole-size-real 14.0)
(def nail-height 1.4)
(def nail-width 5.0)
(def nail-size 0.5)

(def hole-size-err (* hole-size-real 0.05))

(def hole-size (+ hole-size-real hole-size-err))

(def hole-height 3.0)
(def wall-width 2.25)

(def corner-height 2.0)
(def corner-delta 0.01)

(def mount-hole
  "mount hole object for cherry MX switch"
  (let [square-out (square (+ hole-size (* wall-width 2))
                           (+ hole-size (* wall-width 2))
                           :center true)
        square-hole (square hole-size hole-size :center true)
        square-hollow (difference square-out square-hole)

        hollow-box (extrude-linear {:height hole-height :center false}
                                   square-hollow)

        nail-void-height (max (- hole-height nail-height) 0)

        square-nail-void (->> (square nail-size nail-width :center true)
                              (translate [(/ 2 (+ hole-size nail-size)) 0 0]))
        nail-void-oneside (extrude-linear {:height nail-void-height :center false}
                                          square-nail-void)
        nail-void (union nail-void-oneside
                         (mirror [1 0 0] nail-void-oneside))

        mount-hole (->> (difference hollow-box
                                    nail-void)
                        (translate [0 0 (- hole-height)]))]
    mount-hole))

(def mount-corners
  "corner object for cherry MX switch mount hole"
  (let [displace (+ (/ hole-size 2) wall-width)
        element (->> (cylinder corner-delta hole-height :center true)
                     (with-fn 16))]
    {:left-down (translate
                 [(- displace) (- displace) (- (/ hole-height 2))]
                 element)
     :right-down (translate
                  [displace (- displace) (- (/ hole-height 2))]
                  element)
     :right-up (translate
                [displace displace (- (/ hole-height 2))]
                element)
     :left-up (translate
               [(- displace) displace (- (/ hole-height 2))]
               element)}))
