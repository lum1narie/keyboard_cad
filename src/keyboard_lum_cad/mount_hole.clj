(ns keyboard-lum-cad.mount-hole
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :as model]))

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
  (let [square-out (model/square (+ hole-size (* wall-width 2))
                                 (+ hole-size (* wall-width 2))
                                 :center true)
        square-hole (model/square hole-size hole-size :center true)
        square-hollow (model/difference square-out square-hole)

        hollow-box (model/extrude-linear {:height hole-height :center false}
                                         square-hollow)

        nail-void-height (max (- hole-height nail-height) 0)

        square-nail-void (->> (model/square nail-size nail-width :center true)
                              (model/translate
                               [(/ 2 (+ hole-size nail-size)) 0 0]))
        nail-void-oneside (model/extrude-linear {:height nail-void-height
                                                 :center false}
                                                square-nail-void)
        nail-void (model/union nail-void-oneside
                               (model/mirror [1 0 0] nail-void-oneside))

        mount-hole (->> (model/difference hollow-box
                                          nail-void)
                        (model/translate [0 0 (- hole-height)]))]
    mount-hole))

(def mount-corners
  "corner object for cherry MX switch mount hole"
  (let [displace (+ (/ hole-size 2) wall-width)

        element (model/cube corner-delta corner-delta corner-height
                            :center true)]
    {:left-down (model/translate
                 [(- displace) (- displace) (- (/ hole-height 2))]
                 element)
     :right-down (model/translate
                  [displace (- displace) (- (/ hole-height 2))]
                  element)
     :right-up (model/translate
                [displace displace (- (/ hole-height 2))]
                element)
     :left-up (model/translate
               [(- displace) displace (- (/ hole-height 2))]
               element)}))

(def mount-test
  "test object of mugen"
  (model/union mount-hole
               (->> (model/hull (mount-corners :left-down)
                                (mount-corners :right-down)
                                (mount-corners :right-up)
                                (mount-corners :left-up))
                    (model/translate [30 0 0]))))
