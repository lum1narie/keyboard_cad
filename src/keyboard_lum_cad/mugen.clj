(ns keyboard-lum-cad.mugen
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :as model]))

;; variables for mugen
;;
(def mugen-size-real 18.5)
(def mugen-height-real 1.5)

(def burr-length 1.0)
(def burr-width 2.0) ;; actual size is 1
(def socket-height-real 1.3)

(def wall-width 1.5)
(def thin-base-height 1.5)

(def mugen-size-err (* mugen-size-real 0.025))
(def mugen-height-err (* mugen-height-real 0.025))
(def socket-height-err (* socket-height-real 0.025))

(def mugen-size (+ mugen-size-real mugen-size-err))
(def mugen-height (+ mugen-height-real mugen-height-err))
(def socket-height (+ socket-height-err socket-height-real))

;; variables for cover
;;
(def cover-delta 0.001)

(def mugen-cover
  "cover object for \"mugen no kanosei ConTaiNeR\""
  (let [outer-height (+ mugen-height socket-height thin-base-height)
        outer-width (+ mugen-size (* wall-width 2))
        hole-width mugen-size

        outer-box (model/cube outer-width outer-width outer-height :center true)
        outer-void (model/cube hole-width
                               hole-width
                               (+ outer-height cover-delta)
                               :center true)
        burr-shape (model/cube burr-width
                               burr-length
                               (+ mugen-height (* cover-delta 2))
                               :center true)

        burr-cut (model/union
                  (->> burr-shape
                       (model/translate [(/ (+ mugen-size burr-length) 2)
                                         -1.75
                                         (-> (- outer-height mugen-size)
                                             (/ 2) (+ cover-delta))])
                       (model/rotate [0 0 (/ model/pi 2)]))
                  (->> burr-shape
                       (model/translate [(- (/ (+ mugen-size burr-length) 2))
                                         -1.75
                                         (-> (- outer-height mugen-size)
                                             (/ 2) (+ cover-delta))])
                       (model/rotate [0 0 (/ model/pi 2)]))
                  (->> burr-shape
                       (model/translate [-6.25
                                         (/ (+ mugen-size burr-length) 2)
                                         (-> (- outer-height mugen-size)
                                             (/ 2) (+ cover-delta))]))
                  (->> burr-shape
                       (model/translate [-6.25
                                         (- (/ (+ mugen-size burr-length) 2))
                                         (-> (- outer-height mugen-size)
                                             (/ 2) (+ cover-delta))]))
                  (->> burr-shape
                       (model/translate [6.75
                                         (/ (+ mugen-size burr-length) 2)
                                         (-> (- outer-height mugen-size)
                                             (/ 2) (+ cover-delta))]))
                  (->> burr-shape
                       (model/translate [6.75
                                         (- (/ (+ mugen-size burr-length) 2))
                                         (-> (- outer-height mugen-size)
                                             (/ 2) (+ cover-delta))])))

        outer (->> (model/difference outer-box outer-void burr-cut)
                   (model/translate [0 0 (- (/ outer-height 2))]))

        cover-end (/ (+ hole-width (/ wall-width 2)) 2)

        loc-x [-6.75 -2.25 6.75]
        loc-y [(- cover-end) -6.75 6.75 cover-end]
        lower-cover (->> (model/polygon [[(get loc-x 1) (get loc-y 3)]
                                         [(get loc-x 1) (get loc-y 2)]
                                         [(get loc-x 0) (get loc-y 2)]
                                         [(get loc-x 0) (get loc-y 1)]
                                         [(get loc-x 1) (get loc-y 1)]
                                         [(get loc-x 1) (get loc-y 0)]
                                         [(get loc-x 2) (get loc-y 0)]
                                         [(get loc-x 2) (get loc-y 3)]])
                         (model/mirror [1 0 0])
                         (model/extrude-linear {:height thin-base-height
                                                :center false})
                         (model/translate [0 0 (- outer-height)]))

        hic-x [(- cover-end) -6.75 -3.25 -2.25 2.25 4.25 6.75 cover-end]
        hic-y [2.25 3.25 6.75 7.25 cover-end]
        higher-cover (->> (model/union (model/polygon
                                        [[(get hic-x 0) (get hic-y 1)]
                                         [(get hic-x 0) (get hic-y 0)]
                                         [(get hic-x 2) (get hic-y 0)]
                                         [(get hic-x 2) (get hic-y 2)]
                                         [(get hic-x 1) (get hic-y 2)]
                                         [(get hic-x 1) (get hic-y 1)]])
                                       (model/polygon
                                        [[(get hic-x 3) (get hic-y 4)]
                                         [(get hic-x 3) (get hic-y 3)]
                                         [(get hic-x 5) (get hic-y 3)]
                                         [(get hic-x 5) (get hic-y 1)]
                                         [(get hic-x 4) (get hic-y 1)]
                                         [(get hic-x 4) (get hic-y 0)]
                                         [(get hic-x 7) (get hic-y 0)]
                                         [(get hic-x 7) (get hic-y 1)]
                                         [(get hic-x 6) (get hic-y 1)]
                                         [(get hic-x 6) (get hic-y 4)]]))
                          (model/mirror [1 0 0])
                          (model/extrude-linear
                           {:height
                            (- (+ socket-height thin-base-height) cover-delta)
                            :center false})
                          (model/translate
                           [0 0 (+ (- outer-height) cover-delta)]))

        cover (model/union lower-cover higher-cover)]
    (model/union outer cover)))

;; variables for corner
;;
(def corner-height 3.0)
(def corner-delta 0.01)

(def mugen-corners
  "corner object for \"mugen no kanosei ConTaiNeR\""
  (let [cover-height (+ mugen-height socket-height thin-base-height)
        displace (+ (/ mugen-size 2) wall-width)

        element (model/cube corner-delta corner-delta corner-height
                            :center true)]
    {:left-down (model/translate
                 [(- displace) (- displace) (- (/ cover-height 2))]
                 element)
     :right-down (model/translate
                  [displace (- displace) (- (/ cover-height 2))]
                  element)
     :right-up (model/translate
                [displace displace (- (/ cover-height 2))]
                element)
     :left-up (model/translate
               [(- displace) displace (- (/ cover-height 2))]
               element)}))

(def mugen-test
  "test object of mugen"
  (model/union mugen-cover
               (->> (model/hull (mugen-corners :left-down)
                                (mugen-corners :right-down)
                                (mugen-corners :right-up)
                                (mugen-corners :left-up))
                    (model/translate [30 0 0]))))
