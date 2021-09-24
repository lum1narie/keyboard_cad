(ns keyboard-lum-cad.keycap-mock
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

(def mock-delta 0.01)
(def keycap-mock
  "mockup of cherry MX keycap"
  (let [bottom (cube 18.0 18.0 mock-delta :center true)
        top (->> (cube 11.0 12.0 mock-delta :center true)
                 (translate [0 0 7.0]))]
    (hull bottom top)))
