#!/bin/sh
# Builds and signs the icon pack. usage: build.sh <font.ttf> <codepoints> <python-with-pillow> <out-dir>
set -e
D=$(cd "$(dirname "$0")" && pwd); FONT=$1; CPS=$2; PY=$3; OUT=$4
SDK=/opt/homebrew/share/android-commandlinetools; BT=$SDK/build-tools/34.0.0; export JAVA_HOME=/opt/homebrew/opt/openjdk@21; export PATH=$JAVA_HOME/bin:$PATH
rm -rf "$OUT"; mkdir -p "$OUT/res" "$OUT/compiled"
"$PY" "$D/gen_icons.py" "$FONT" "$CPS" "$OUT/res"
"$BT/aapt2" compile --dir "$OUT/res" -o "$OUT/compiled/res.zip"
"$BT/aapt2" link -o "$OUT/pack-unsigned.apk" -I "$SDK/platforms/android-34/android.jar" --manifest "$D/AndroidManifest.xml" "$OUT/compiled/res.zip"
"$BT/zipalign" -p -f 4 "$OUT/pack-unsigned.apk" "$OUT/pack-aligned.apk"
"$BT/apksigner" sign --ks "${KS:-$HOME/.android/debug.keystore}" --ks-pass "pass:${KS_PASS:-android}" --out "$OUT/CarDarkIcons.apk" "$OUT/pack-aligned.apk"
"$BT/apksigner" verify "$OUT/CarDarkIcons.apk" && ls -la "$OUT/CarDarkIcons.apk"
