#! /bin/sh

set -x
lein run
lein_result=$?
set +x

if [ $lein_result -gt 0 ]; then
    echo "clojure build failed"
    exit 1
fi

for scadfile in `find things/ -maxdepth 1 -type f -name *.scad` ; do
    set -x
    openscad $scadfile -o ${scadfile%.scad}.stl
    set +x
done

for scadfile in `find things/parts/ -maxdepth 1 -type f -name *.scad` ; do
    set -x
    openscad $scadfile -o ${scadfile%.scad}.stl
    set +x
done
