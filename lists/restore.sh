#!/bin/sh
# Re-enable every package in a debloat list. usage: restore.sh <unit ip[:port]> [list file]
S="${1:?unit ip}"; case "$S" in *:*) ;; *) S="$S:5555";; esac
L="${2:-$(dirname "$0")/bwic-yato1-b100-debloat.txt}"
adb connect "$S" >/dev/null
for p in $(grep -v '^#' "$L" | grep .); do
  printf '%s: ' "$p"; adb -s "$S" shell pm enable --user 0 "$p" </dev/null
done
