(ns keyboard-lum-cad.screw
  (:refer-clojure :exclude [+ - * / == < <= > >= not= = min max])
  (:require [scad-clj.model :as model]
            [clojure.core.matrix.operators :refer :all]))

(defn screw-top
  "get top screw object"
  [screw-r-real screw-head-r-real
   head-height foot-height neck-height
   side-thickness side-inner-thickness-real]
  (let [screw-r-err (* screw-r-real 0.1)
        screw-head-r-err (* screw-head-r-real 0.1)
        side-inner-thickness-err (* side-inner-thickness-real 0.1)

        screw-r (+ screw-r-real screw-r-err)
        screw-head-r (+ screw-head-r-real screw-head-r-err)
        side-inner-thickness (+ side-inner-thickness-real
                                side-inner-thickness-err)

        whole-height (+ head-height neck-height foot-height)

        outer-2d (->> (model/square (+ side-inner-thickness side-thickness)
                                    whole-height
                                    :center false)
                      (model/translate [screw-r (- whole-height)]))
        head-void-2d (->> (model/square screw-head-r head-height :center false)
                          (model/translate [0 (- head-height)]))
        bottom-void-2d (->> (model/square side-inner-thickness
                                          foot-height
                                          :center false)
                            (model/translate [screw-r (- whole-height)]))

        screw-2d (model/difference outer-2d
                                   head-void-2d
                                   bottom-void-2d)]
    (->> (model/extrude-rotate screw-2d)
         (model/with-fn 32))))

(defn screw-bottom
  "get bottom screw object"
  [screw-r-real nut-r-real
   nut-height foot-height neck-height
   side-thickness]
  (let [delta 0.01

        screw-r-err (* screw-r-real 0.1)
        nut-r-err (* nut-r-real 0.1)

        screw-r (+ screw-r-real screw-r-err)
        nut-r (+ nut-r-real nut-r-err)

        foot-2d (->> (model/square side-thickness
                                   foot-height
                                   :center false)
                     (model/translate [screw-r (+ neck-height nut-height)]))
        nut-outer-2d (->> (model/square (-> (+ nut-r side-thickness)
                                            (- screw-r))
                                        (+ nut-height neck-height)
                                        :center false)
                          (model/translate [screw-r 0]))

        screw-without-nut-2d (model/union foot-2d nut-outer-2d)
        screw-without-nut (->> (model/extrude-rotate screw-without-nut-2d)
                               (model/with-fn 32))

        nut-void (->> (model/cylinder nut-r (+ nut-height delta) :center false)
                      (model/with-fn 6)
                      (model/translate [0 0 (- delta)]))]
    (model/difference screw-without-nut nut-void)))

(defrecord screw-pair [top bottom distance])
(defn make-screw-pair
  "get screw pair object"
  [& {:keys [screw-r screw-head-r nut-r distance
             screw-head-height nut-height top-neck-height bottom-neck-height
             top-side-thickness bottom-side-thickness]
      :or {screw-r (/ 3.0 2)
           screw-head-r  (/ 6.0 2)
           nut-r  (/ 6.0 2)
           distance  40.0
           screw-head-height  3.0
           nut-height  4.0
           top-neck-height  7.5
           bottom-neck-height  7.5
           top-side-thickness  2.0
           bottom-side-thickness  2.0}}]
  (let [foot-height (- distance
                       screw-head-height
                       top-neck-height
                       bottom-neck-height
                       nut-height)
        top (screw-top screw-r screw-head-r
                       screw-head-height foot-height top-neck-height
                       top-side-thickness bottom-side-thickness)
        bottom (screw-bottom screw-r nut-r
                             nut-height foot-height bottom-neck-height
                             bottom-side-thickness)]
    (->screw-pair top bottom distance)))

(defn deploy-screw-pair
  "place screw pair as objects"
  [pair]
  (model/union (:top pair)
               (->> (:bottom pair)
                    (model/translate
                     [0 0 (-> (+ (:distance pair) 2) (-))]))))

(def screw-pair-test
  "test objects from screw pair"
  (let [pair (make-screw-pair)]
    (deploy-screw-pair pair)))
