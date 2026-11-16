#!/bin/sh
# Disable the packages in a list, one shell call each. usage: disable.sh <unit ip[:port]> <list file>
S="${1:?unit ip}"; case "$S" in *:*) ;; *) S="$S:5555";; esac; L="${2:?list file}"
for p in $(grep -v '^#' "$L" | grep .); do
  case "$p" in com.bw.*|com.autochips.*|android|com.android.systemui|com.android.settings|com.android.shell|com.android.phone|com.android.providers.*)
    echo "REFUSED (car-critical or core): $p"; continue;; esac
  printf '%s: ' "$p"; adb -s "$S" shell pm disable-user --user 0 "$p" </dev/null
done
