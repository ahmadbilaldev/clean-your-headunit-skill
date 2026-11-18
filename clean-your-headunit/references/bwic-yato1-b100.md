# BWIC Yato1 B100 (Autochips AC8257, Android 9): what is known

Read this when the unit's `ro.build.display.id` starts with `OS_Yato1`, or when its package list is full
of `com.bw.*` and `com.autochips.*`.

## Facts
- userdebug build. ADB over Wi-Fi on TCP 5555.
- 1280x720, density 320, landscape only. 1.8 GB RAM, slow storage: allow 60-90 s for a reboot.
- Key-off is sleep. Only `adb reboot` clears process state. Wi-Fi can drop for minutes without a reboot;
  compare `uptime` before assuming the unit restarted.
- Logcat keeps only error-level lines.

## Never disable
`com.bw.car`, `com.bw.carsetting`, `com.bw.source`, `com.bw.settings`, `com.bw.mediaplayer`, `com.bw.phone`,
`com.bw.adas`, `com.bw.personalization`, `com.bw.asr`, `com.bw.ipod`, `com.bw.userguide`, every
`com.autochips.*`, `mdm.tw.com.mdm`, `com.example` (AutoDialer). Ask about phone-link apps:
`net.easyconn`, `cn.manstep.phonemirrorBox`.

## Safe on the tested unit
See `lists/bwic-yato1-b100-debloat.txt` in the repo. Batch 3 (TXZ) silences the voice button.

## Settings worth knowing
- Navi button: `secure navigation_component_bw` (component) and `global bw_navi_app_name` (label).
- Current audio source: `system bw_current_source_name`.
- Vendor key beep is BwCarService "Key prompt" in the vendor settings app.

## Cannot be changed without flashing
Boot logo and boot animation (in a write-protected partition), Play Protect certification.
