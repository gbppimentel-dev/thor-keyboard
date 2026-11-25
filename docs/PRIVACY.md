# Privacy and permissions

The app has no Internet permission, analytics SDK, account system, prediction server or typed-text history. Local keyboard sounds are bundled. Paste uses the current input connection only when requested. As with any Android input method, the keyboard processes the text you choose to type; review the source and Android's keyboard permission prompt before enabling it.

- **Input method:** handles the editor's input connection and controller/touch input.
- **Display overlay:** renders the keyboard panel on the other screen without stealing focus.
- **Accessibility:** The standalone optional service only hosts a keyboard overlay. It does not request retrieval of window contents or screenshots, and it does not manage controller focus.

Build/signing keys, device dumps, personal configuration backups and historic screenshots are not included. Local build products are ignored. Do not attach private typed text, clipboard data, signing credentials or complete device backups to a public issue.
