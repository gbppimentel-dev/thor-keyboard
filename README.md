# Thor Keyboard

A standalone **touch and controller keyboard for AYN Thor**, extracted from [Thor Tools](https://github.com/gbppimentel-dev/thor-tools). Type on one screen while keeping the keyboard on the other.

[Setup](docs/SETUP.md) · [Controller mapping](docs/CONTROLS.md) · [Wayfinder coexistence](docs/WAYFINDER.md) · [Build](docs/BUILD.md) · [Privacy](docs/PRIVACY.md)

## Features

- Opens opposite the text field: editor on top → keyboard on bottom; editor on bottom → keyboard on top.
- **Top screen:** split left/right thumb layout, including numbers and symbols, with Space on each half.
- **Bottom screen:** normal full-width keyboard, with a minimum height of 60% of the display.
- Immediate touch highlight and enlarged character preview on finger-down; both clear on release/cancel.
- Controller navigation, A to type, B to hide, X to delete, Y for Shift/hold Caps, and R1 to swap screens.
- Distinct Shift/Caps indicators and local sounds for special actions. Navigation is quieter and has no typing pulse.
- Dark rounded keys with colored controller badges, keeping the typing layout uncluttered.
- No prediction, account, network access, emoji service or typed-text history.

## Standalone identity

- App: **Thor Keyboard**
- Package: `dev.gbppimentel.thorkeyboard`
- Input method: `.CompactKeyboard`
- Initial standalone version: **1.0.0**, derived from Thor Tools 1.28.

The standalone package has its own preferences and setup screen. It does not include Thor Tools' guide pause, focus border, app dashboard or emulator reference. It can coexist with Thor Tools, but only one Android input method is selected at a time.

## Install and enable

1. [Build the APK](docs/BUILD.md) and install it.
2. Open **Thor Keyboard**, then **Enable Thor Keyboard**.
3. Use **Choose Keyboard** to select it. The app never changes the selected input method on its own.
4. Allow the keyboard overlay for cross-screen rendering. An optional accessibility overlay host is also available; it does not retrieve app contents or manage controller focus.
5. Tap the preview field to type. Set keyboard height in the app. R1 swaps the keyboard between screens.

Without usable overlay access, the keyboard falls back to the editor's screen. See [setup](docs/SETUP.md).

## Works alongside Wayfinder

[Wayfinder](https://github.com/Thor-Wayfinder/thor-wayfinder) handles screen management and configurable shortcuts. Thor Keyboard supplies an Android input method. The app provides an **Open Wayfinder** shortcut, and [the integration guide](docs/WAYFINDER.md) explains the distinction between this keyboard and Wayfinder's own keyboard/trackpad deck.

No Wayfinder source is copied, no upstream keyboard is replaced, and no upstream contribution or endorsement is implied.

## Build and status

```sh
export ANDROID_SDK_ROOT=/path/to/android-sdk
export JAVA_HOME=/path/to/jdk-21
python3 build.py
```

Output: `dist/ThorKeyboard-debug.apk`. The app uses only Android framework APIs and bundled original sound samples.

The extracted standalone package is new. It is checked by local compilation, APK signing verification and manifest/resource inspection; it has not been installed or runtime-tested as a separate package on the user's Thor. The original bundled keyboard has prior on-device use. Display IDs 0 and 4 and AYN Thor are the intended target; other devices are unvalidated.

## Reporting issues

Include the app version, firmware, editor display, keyboard display and whether touch or a controller triggered the issue. Do not include passwords, private typed text, signing files or device backups.

See [NOTICE](NOTICE.md) for attribution and licensing status. The public source currently has no selected reuse license.
