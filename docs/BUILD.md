# Building and signing

## Requirements

- Python 3, JDK 17+ (JDK 21 is used for local verification).
- Android SDK platform `android-36` and build-tools `36.0.0`.
- Set `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) and `JAVA_HOME`.

```sh
sdkmanager "platforms;android-36" "build-tools;36.0.0"
python3 build.py
```

`ANDROID_PLATFORM` and `ANDROID_BUILD_TOOLS_VERSION` override the SDK choices. The script compiles Java, runs D8, packages resources and uncompressed WAV samples, aligns the APK, signs it and verifies the signature. Build outputs and debug keys stay under ignored `build/` and `dist/` directories.

## Debug builds

The script creates a local disposable debug keystore. The conventional debug password is not a release credential. Do not distribute its private key as a production signing identity. Debug APKs cannot update an installed APK signed by a different key; Android will reject the update.

## Release builds

Provide your own private keystore and keep it outside the repository:

```sh
export SIGNING_KEYSTORE=/private/path/release.jks
export SIGNING_KEY_ALIAS=your-key-alias
# Supply SIGNING_STORE_PASSWORD through your shell or secret manager.
# Supply SIGNING_KEY_PASSWORD if the key password differs.
python3 build.py --release
```

Do not paste signing passwords into README files, command history or issue reports. `apksigner` reads release passwords from environment variables. This repository includes no production key and no device-specific signing password.

## GitHub Actions

The build workflow produces a debug APK artifact using a fresh runner-local key. It does not publish a release or access a production signing secret. A fresh key may differ between runs; those debug artifacts are for evaluation, not an upgrade channel for an existing signed installation.
