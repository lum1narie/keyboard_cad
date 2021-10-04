(ns keyboard-lum-cad.core
  (:gen-class)
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :as scad]
            [keyboard-lum-cad.mugen :as mugen]
            [keyboard-lum-cad.mount-hole :as mhole]
            [keyboard-lum-cad.keycap-mock :as kmock]
            [keyboard-lum-cad.screw :as screw]
            [keyboard-lum-cad.keyboard :as kb]))

(defn part-mugen []
  (spit "things/parts/mugen.scad"
        (scad/write-scad mugen/mugen-test))
  (println "wrote part mugen.scad"))
(defn part-mount []
  (spit "things/parts/mount-hole.scad"
        (scad/write-scad mhole/mount-test))
  (println "wrote part mount-hole.scad"))
(defn part-keymock []
  (spit "things/parts/keycap-mock.scad"
        (scad/write-scad kmock/keycap-mock))
  (println "wrote part keycap-mock.scad"))
(defn part-screw []
  (spit "things/parts/screw.scad"
        (scad/write-scad screw/screw-pair-test))
  (println "wrote part keycap-mock.scad"))
(defn keyboard []
  (spit "things/keyboard.scad"
        (scad/write-scad kb/keyboard-test))
  (println "wrote part keyboard.scad"))

(def generators
  {"keyboard" keyboard
   "mugen" part-mugen
   "mount-hole" part-mount
   "keycap-mock" part-keymock
   "screw" part-screw})

(defn nop [] nil)

(defn -main
  [& args]
  (.mkdirs (java.io.File. "things/parts/"))
  (if (empty? args)
    (dorun (map #(%) (vals generators)))
    (dorun (map #((generators % nop)) args))))
