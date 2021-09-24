(ns keyboard-lum-cad.core
  (:gen-class)
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [keyboard-lum-cad.mugen :as mugen]
            [keyboard-lum-cad.mount-hole :as mhole]
            [keyboard-lum-cad.keycap-mock :as kmock]))

(defn -main
  [& _args]
  (.mkdirs (java.io.File. "things/parts/"))
  (spit "things/parts/mugen.scad"
        (write-scad mugen/mugen-test))
  (spit "things/parts/mount-hole.scad"
        (write-scad mhole/mount-test))
  (spit "things/parts/keycap-mock.scad"
        (write-scad kmock/keycap-mock)))
