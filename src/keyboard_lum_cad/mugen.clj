(ns keyboard-lum-cad.mugen
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

;; variables for mugen

(def mugen-size-real 18.5)
(def mugen-height-real 1.5)

(def burr-length 1)
(def burr-width 2) ;; actual size is 1
(def socket-height-real 1.3)

(def mugen-size-err (* mugen-size-real 0.025))

(def mugen-height-err (* mugen-height-real 0.025))
(def socket-height-err (* socket-height-real 0.025))

;; variables for cover

(def wall-width 1.5)
(def thin-base-height 1.5)
(def cover-delta 0.001)

;; varibles for TODO:

(def mugen-cover
  "cover object for \"mugen no kanosei ConTaiNeR\""
  (let [mugen-size (+ mugen-size-real mugen-size-err)
        mugen-height (+ mugen-height-real mugen-height-err)
        socket-height (+ socket-height-err socket-height-real)

        outer-height (+ mugen-height socket-height thin-base-height)
        outer-width (+ mugen-size (* wall-width 2))
        hole-width mugen-size

        outer-box (cube outer-width outer-width outer-height :center true)
        outer-void (cube hole-width hole-width (+ outer-height cover-delta)
                         :center true)
        burr-shape (cube burr-width
                         burr-length
                         (+ mugen-height (* cover-delta 2))
                         :center true)

        burr-cut (union
                  (->> burr-shape
                       (translate [(/ (+ mugen-size burr-length) 2)
                                   -1.75
                                   (-> (- outer-height mugen-size)
                                       (/ 2) (+ cover-delta))])
                       (rotate [0 0 (/ pi 2)]))
                  (->> burr-shape
                       (translate [(- (/ (+ mugen-size burr-length) 2))
                                   -1.75
                                   (-> (- outer-height mugen-size)
                                       (/ 2) (+ cover-delta))])
                       (rotate [0 0 (/ pi 2)]))
                  (->> burr-shape
                       (translate [-6.25
                                   (/ (+ mugen-size burr-length) 2)
                                   (-> (- outer-height mugen-size)
                                       (/ 2) (+ cover-delta))]))
                  (->> burr-shape
                       (translate [-6.25
                                   (- (/ (+ mugen-size burr-length) 2))
                                   (-> (- outer-height mugen-size)
                                       (/ 2) (+ cover-delta))]))
                  (->> burr-shape
                       (translate [6.75
                                   (/ (+ mugen-size burr-length) 2)
                                   (-> (- outer-height mugen-size)
                                       (/ 2) (+ cover-delta))]))
                  (->> burr-shape
                       (translate [6.75
                                   (- (/ (+ mugen-size burr-length) 2))
                                   (-> (- outer-height mugen-size)
                                       (/ 2) (+ cover-delta))])))

        outer (->> (difference outer-box outer-void burr-cut)
                   (translate [0 0 (- (/ outer-height 2))]))

        cover-end (/ (+ hole-width (/ wall-width 2)) 2)

        loc-x [-6.75 -2.25 6.75]
        loc-y [(- cover-end) -6.75 6.75 cover-end]
        lower-cover (->> (polygon [[(get loc-x 1) (get loc-y 3)]
                                   [(get loc-x 1) (get loc-y 2)]
                                   [(get loc-x 0) (get loc-y 2)]
                                   [(get loc-x 0) (get loc-y 1)]
                                   [(get loc-x 1) (get loc-y 1)]
                                   [(get loc-x 1) (get loc-y 0)]
                                   [(get loc-x 2) (get loc-y 0)]
                                   [(get loc-x 2) (get loc-y 3)]])
                         (extrude-linear {:height thin-base-height
                                          :center false})
                         (translate [0 0 (- outer-height)]))

        hic-x [(- cover-end) -6.75 -3.25 -2.25 2.25 4.25 6.75 cover-end]
        hic-y [2.25 3.25 6.75 7.25 cover-end]
        higher-cover (->> (union (polygon [[(get hic-x 0) (get hic-y 1)]
                                           [(get hic-x 0) (get hic-y 0)]
                                           [(get hic-x 2) (get hic-y 0)]
                                           [(get hic-x 2) (get hic-y 2)]
                                           [(get hic-x 1) (get hic-y 2)]
                                           [(get hic-x 1) (get hic-y 1)]])
                                 (polygon [[(get hic-x 3) (get hic-y 4)]
                                           [(get hic-x 3) (get hic-y 3)]
                                           [(get hic-x 5) (get hic-y 3)]
                                           [(get hic-x 5) (get hic-y 1)]
                                           [(get hic-x 4) (get hic-y 1)]
                                           [(get hic-x 4) (get hic-y 0)]
                                           [(get hic-x 7) (get hic-y 0)]
                                           [(get hic-x 7) (get hic-y 1)]
                                           [(get hic-x 6) (get hic-y 1)]
                                           [(get hic-x 6) (get hic-y 4)]]))
                          (extrude-linear
                           {:height
                            (- (+ socket-height thin-base-height) cover-delta)
                            :center false})
                          (translate [0 0 (+ (- outer-height) cover-delta)]))

        cover (union lower-cover higher-cover)]
    (union outer cover)))
