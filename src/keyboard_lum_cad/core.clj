(ns keyboard-lum-cad.core
  (:gen-class)
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :as scad]
            [keyboard-lum-cad.mugen :as mugen]
            [keyboard-lum-cad.mount-hole :as mhole]
            [keyboard-lum-cad.keycap-mock :as kmock]))

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

(def generators
  {"mugen" part-mugen
   "mount-hole" part-mount
   "keycap-mock" part-keymock})

(defn nop [] nil)

(defn -main
  [& args]
  (.mkdirs (java.io.File. "things/parts/"))
  (if (empty? args)
    (dorun (map #(%) (vals generators)))
    (dorun (map #((generators % nop)) args))))
