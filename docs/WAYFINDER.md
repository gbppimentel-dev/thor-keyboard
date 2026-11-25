# Wayfinder coexistence and links

- Upstream: [Thor-Wayfinder/thor-wayfinder](https://github.com/Thor-Wayfinder/thor-wayfinder)
- Companion app: [Thor Tools](https://github.com/gbppimentel-dev/thor-tools)

Wayfinder and Thor Keyboard are separate applications. Wayfinder owns its own keyboard/trackpad deck and controller/shortcut system. Thor Keyboard is selected through Android's input-method picker and types into the current editor's input connection.

## Setup

1. Install and configure Wayfinder using its own instructions.
2. Install Thor Keyboard, enable it in Android keyboard settings, and select it.
3. Allow a cross-screen overlay, or enable the optional accessibility overlay host if appropriate for your setup.
4. Use Wayfinder for app movement/controller focus. Use R1 inside Thor Keyboard to move the keyboard itself.

The **Open Wayfinder** button opens the installed app, or its upstream GitHub project if the package is absent. This is a launch/documentation link. The project does not claim a private Wayfinder keyboard API or automatically rewrite Wayfinder's shortcuts. In the original setup, Home + Y opens Wayfinder's own deck; it does not launch this standalone IME. If your Wayfinder version exposes an input-method-picker action, you can bind that action using its normal shortcut editor.

Only one IME is selected at a time. Installing the standalone package leaves the bundled Compact Keyboard and Wayfinder Keyboard available; choose the one you intend to use. No Wayfinder code, assets or license files are redistributed here.
