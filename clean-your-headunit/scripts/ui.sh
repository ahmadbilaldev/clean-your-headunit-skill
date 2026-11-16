#!/bin/sh
# Print "(x,y) [c] label" for every labelled view on the unit's screen. usage: ui.sh <unit ip[:port]>
S="${1:?unit ip}"; case "$S" in *:*) ;; *) S="$S:5555";; esac
T=$(mktemp); D=$(cd "$(dirname "$0")" && pwd)
adb -s "$S" shell "rm -f /sdcard/Pictures/.ui.xml; uiautomator dump /sdcard/Pictures/.ui.xml >/dev/null 2>&1; cat /sdcard/Pictures/.ui.xml" > "$T" 2>/dev/null
python3 "$D/ui.py" "$T"; rm -f "$T"
