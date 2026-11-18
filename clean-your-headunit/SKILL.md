---
name: clean-your-headunit
description: Debloat and restyle an aftermarket Android car head unit (Autochips, MediaTek, BWIC "Yato", generic Android 9-11 radios) over ADB without root. Use this whenever someone wants to clean up, speed up, debloat or re-skin a car infotainment screen, car stereo, car radio or "Android head unit", change its launcher, icons, wallpaper, widgets or navigation button, or stop its nags and prompts, even if they only say "my car screen is slow" or "remove the junk apps from my car".
---

# Clean your head unit

A head unit is an Android tablet wired to the car's amplifier, steering-wheel keys and cameras. The
vendor apps that look like bloat are often the radio, the reverse camera or the power manager. So the
whole job is disable-only, snapshot-first and reversible, and you check the car still works after
every batch.

## Rules that are not negotiable

- **Disable, never uninstall** system packages: `pm disable-user --user 0 <pkg>`, one per call. No root,
  nothing flashed. Uninstalling is only for apps you installed yourself.
- **Snapshot first** with `scripts/snapshot.sh`. The snapshot holds MAC addresses and phone numbers,
  so it never goes into git.
- **Never touch car-critical packages.** Anything from the board vendor (`com.bw.*`, `com.autochips.*`,
  `com.mediatek.*` services, the vendor launcher, the `mdm`/`mcu`/`canbus` services) stays enabled
  unless `dumpsys package` proves it is only a consumer app. `scripts/disable.sh` refuses the known ones.
- **Keep a Home fallback.** Leave the vendor launcher enabled when you install another one.
- **Ask the owner one checklist question** before disabling anything they might use: phone-link apps
  (CarPlay / Android Auto dongle apps, EasyConnection), the voice assistant, maps, media apps.
- **Report what you verified and what you did not.** A screenshot of the result is evidence; a tool
  call returning "true" is not.

## Workflow

1. **Find and identify.** Ask for the unit's IP (its Wi-Fi settings show it) and `adb connect <ip>:5555`.
   Read `getprop ro.build.display.id ro.product.model ro.build.version.release ro.build.type`,
   `wm size`, `wm density`, and the package list. If it is a BWIC Yato unit, read
   `references/bwic-yato1-b100.md`.
2. **Snapshot** into a gitignored folder. Write a restore script next to the list you will apply.
3. **Classify.** For each non-obvious package read `dumpsys package <pkg>`: persistent flag, shared
   system uid, services, receivers for boot or car events. Sort into never-touch, ask-the-owner and
   safe. When unsure, leave it.
4. **Disable in small batches** (unused apps, then telemetry and helpers, then anything with a
   visible cost). After each batch take a screenshot and confirm the car processes are alive
   (`ps -A | grep <vendor prefix>`), there are no new crashes in logcat, and Home still resolves.
5. **Reboot once for real** (`adb reboot`; key-off is usually only sleep) and re-check the disabled
   list. If apps came back, the Play Store re-enabled them: turn its auto-update off.
6. **Restyle if asked.** Launcher, icon pack, wallpaper, widgets. The repo's `apps/` folder has
   buildable examples; adapt the component names in `gen_icons.py` to this unit's launcher activities
   (`cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LAUNCHER`).
7. **Write down** what was applied, what was installed (with its undo line) and what was left.

## Driving the screen remotely

`scripts/ui.sh <ip>` lists labelled views with tap coordinates. Things that save time:

- A launcher's settings list may render blank at high density: `wm density 200`, do the taps,
  then `wm density <original>`. Always reset it.
- Launcher drawers open the keyboard and taps land on keys. Type into the search box, press Back,
  then `input draganddrop sx sy tx ty 1500`.
- `input swipe x y x y 1000` is a long press. Menus appear after release; dump the UI to find them.
- To place a widget from your own app, call `AppWidgetManager.requestPinAppWidget` and tap the
  launcher's confirmation. Restart the launcher if the widget does not appear.
- Dialogs that need the owner's consent (Play Protect "Install anyway", wallpaper pickers that ignore
  injected taps) are for the owner to tap. Tell them it is waiting; such dialogs time out.

## Building small apps without Gradle

`aapt2 compile/link`, `javac -source 1.8 -target 1.8 -bootclasspath android.jar`, `d8`, `zip`,
`zipalign`, `apksigner`. With JDK 21 and build-tools 34, `d8` crashes on inner classes, anonymous
classes and lambdas: use static nested classes with an explicit outer reference. Widgets should draw
bitmaps, because fonts do not cross into the launcher. Log at error level on firmware that drops the
rest.

## Finish

Tell the owner, in plain words: what was disabled and how to undo it, what was installed, what they
must do themselves on the screen, and a reminder to switch network debugging off when finished if
the unit offers that setting.
