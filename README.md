# NOVA — Offline AI Voice Controller for Android

> Your personal voice AI. No internet. No Google. No third-party SDKs. 100% built from scratch.

## Install

**[Download Latest APK](https://github.com/Aniketsingh2104/NOVA/releases/latest)**

Or visit the download page: **https://Aniketsingh2104.github.io/NOVA**

## What is NOVA?

NOVA is a fully offline, always-listening Android voice controller.
It wakes on your custom word, understands natural language using an on-device AI
written entirely from scratch, and controls every part of your Android phone.

## Features

- Custom wake word — trained on your own voice, any word you choose
- 100% offline — works in airplane mode, no server, no cloud
- No third-party SDKs — FFT, MFCC, neural network, STT, TTS all written by us
- Full phone control — calls, apps, volume, navigation, flashlight, screenshot
- Screen vision — reads and taps any element on screen by voice
- Task agent — multi-step autonomous tasks
- PC control over LAN — controls your Windows PC by voice
- Smart home via Home Assistant
- Habit learning — gets smarter the more you use it
- Home screen widget

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| Wake word | Our own MFCC + neural network |
| Speech-to-text | Our own acoustic model + CTC decoder |
| NLU | Our own rule engine + intent classifier |
| TTS | Our own concatenative synthesis |
| Neural net | Our own matrix math + backprop + Adam |
| Phone control | Android AccessibilityService |
| Database | Room |

## Build from source

```bash
git clone https://github.com/Aniketsingh2104/NOVA.git
cd NOVA
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Requirements

- Android 8.0+ (API 26)
- 3 GB RAM minimum
- Microphone
- Accessibility Service enabled (one-time setup)

## License

MIT License — free to use, modify, and distribute.
