# Setup and troubleshooting

Use the app's explicit setup buttons to enable and choose Thor Keyboard. Grant overlay access for cross-screen rendering. The optional accessibility service is only an overlay host; it requests neither app-content retrieval nor screenshot access.

| Problem | Check |
| --- | --- |
| Another keyboard appears | Use Android's keyboard picker; installation never selects this app automatically. |
| Keyboard stays on the editor screen | Check overlay access or the optional accessibility host. Overlay failures fall back locally. |
| Top split or bottom layout looks wrong | Layout follows the actual rendering display, using Thor IDs 0/4. Unsupported firmware/display IDs may need adaptation. |
| No sound | Check system touch sounds and system-effects volume. No app-specific volume is raised. |
| Controller actions go to the wrong app | Check Wayfinder/firmware controller focus. The keyboard does not manage focus lock. |
| App cannot update an installed APK | The package or signing key may differ. Standalone Thor Keyboard and bundled Compact Keyboard are separate packages. |
| Height differs by screen | The height slider is 180–280 dp; bottom has a 60% minimum and both displays have an 88% maximum cap. |

The setup preview does not automatically open the keyboard. Tap it when ready. The top keyboard has a 20% center gap and two Space keys. The bottom keeps the normal layout. Changing screens preserves Caps behavior, local sounds and character previews.
