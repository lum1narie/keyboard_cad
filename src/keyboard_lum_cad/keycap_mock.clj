(ns keyboard-lum-cad.keycap-mock
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :as model]))

(def mock-delta 0.01)
(def keycap-mock
  "mockup of cherry MX keycap"
  (let [bottom (model/cube 18.0 18.0 mock-delta :center true)
        top (->> (model/cube 11.0 12.0 mock-delta :center true)
                 (model/translate [0 0 7.0]))]
    (model/hull bottom top)))
