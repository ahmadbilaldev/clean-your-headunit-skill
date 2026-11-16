#!/bin/sh
# Save the unit's state before any change. usage: snapshot.sh <unit ip[:port]> <out-dir>
# The output holds MAC addresses, phone numbers and account names: keep it out of git.
set -e
S="${1:?unit ip}"; case "$S" in *:*) ;; *) S="$S:5555";; esac; O="${2:?out dir}"; mkdir -p "$O"
adb connect "$S" >/dev/null
adb -s "$S" shell pm list packages -f > "$O/packages-all.txt"
adb -s "$S" shell pm list packages -d > "$O/packages-disabled.txt"
adb -s "$S" shell pm list packages -3 > "$O/packages-3rd.txt"
adb -s "$S" shell getprop > "$O/getprop.txt"
for t in global secure system; do adb -s "$S" shell settings list $t > "$O/settings-$t.txt"; done
wc -l "$O"/*.txt
