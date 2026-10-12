# Shaadi Studio — Native Android app

This is a **native Android app built with Kotlin + Jetpack Compose**. It does not embed the deployed website or use a WebView. The screens, inputs, cards, navigation, share sheet, gallery picker, PDF export, and local persistence are native Android APIs.

## Features
- Native invitation editor and live invitation preview
- Canva-inspired multi-page Canvas Studio with add/duplicate/delete pages, draggable elements, layer reorder, size/color/bold/position/rotation/scale controls, nine backgrounds and per-element Float/Pulse/Sparkle/Rotate effects
- Import personal photos, PNG cartoon art and stickers from the device gallery; add emoji sticker library, emoji couple avatars and cartoon-style emoji sticker presets
- Export all canvas pages to a multi-page PDF and export animated GIF and silent MP4 videos using built-in/native Android media APIs (no paid service or API key)
- Six design themes: Blush Floral, Royal, Minimal, Garden, Midnight, Playful
- Editable groom/bride names, date, time, venue, family line, message, love story, RSVP deadline, hashtag and travel notes
- Add, edit and remove wedding functions
- Tradition presets: Hindu, Muslim, Sikh, Christian, Jain, Buddhist, Parsi/Zoroastrian, Jewish, interfaith and custom
- Ten language choices for invitation authoring (personal text remains editable; UI chrome is currently English)
- Pick and retain local photo/video document URIs
- Guest RSVP list stored on device, guest count, accept/decline/maybe, CSV export
- Save, reopen and delete local invitation drafts
- Native Android share sheet
- Export a printable invitation PDF
- Offline-first: no account, web server, analytics, or network permission needed
- Demo invitation prefilled with Vijay & Rashima

## Build a debug APK
Requires Android Studio, Android SDK 35, JDK 17, and internet for Gradle dependencies.

Open this folder in Android Studio, allow Gradle sync, then choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**. The APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

Or from a terminal:
```bash
gradle assembleDebug
```

## Build through GitHub Actions
1. Create a GitHub repository and upload the contents of this folder.
2. Open **Actions → Build native Android APK → Run workflow** (or push to `main`).
3. Download the `shaadi-studio-native-debug-apk` artifact from the completed workflow run.
4. Transfer `app-debug.apk` to your Android phone and install it. Android may ask you to allow installs from that source.

The generated debug APK is for testing/sideloading. For Google Play, create a signed release AAB/APK and protect the signing key.

## Privacy and limits
All wedding invitation drafts, guest responses and selected gallery URI references are stored locally in app preferences. No data is uploaded. Guest RSVP records are managed by the couple on this device; this version does not create public invitation links or sync responses across devices. Language options help you author content in the selected language, but this version does not auto-translate UI labels or invitation text. Canvas animation previews in-app and exports as animated GIF or silent MP4 (MP4 depends on the phone hardware encoder); AI-generated character creation is not included. Import your own cartoon PNGs or stickers from the device.


## Free/open-source components
- Android Jetpack Compose and Android platform APIs: Apache-2.0 / Android SDK terms.
- Coil Compose for local image rendering: Apache-2.0, https://github.com/coil-kt/coil.
- GIF output uses a small in-project encoder implementation; MP4 uses Android MediaCodec/MediaMuxer. No paid SDKs, API keys, or subscription services are required.
