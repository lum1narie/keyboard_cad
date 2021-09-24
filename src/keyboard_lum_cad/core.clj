(ns keyboard-lum-cad.core
  (:gen-class)
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [keyboard-lum-cad.mugen :refer :all]))

(defn -main
  [& _args]
  (.mkdirs (java.io.File. "things/parts/"))
  (spit "things/parts/mugen.scad"
        (write-scad mugen-test)))
