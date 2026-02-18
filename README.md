# My Launcher

**Version:** 0.1.0  
**Platform:** Android 14+ (API 34)  
**Status:** In Development

---

## What is My Launcher?

An Android phone launcher app with a customizable home screen

---

## Features

- **Customizable home screen** — arrange apps, widgets, and shortcuts freely
- **App drawer** — searchable list of all installed apps
- **Gesture navigation** — swipe gestures for quick actions
- **Widget support** — add and resize Android widgets
- **Icon packs** — support for third-party icon packs
- **Theming** — dynamic colors from Material You / custom themes
- **Folder support** — group apps into folders
- **Search** — universal search for apps, contacts, and web

---

## Getting Started

### Requirements

- Android 14+ (API 34)
- Android Studio Ladybug (2024.2) or later
- JDK 17+

### Build & Run

```bash
# Clone the repo
git clone <repo-url>
cd My Launcher

# Build
./gradlew assembleDebug

# Run
./gradlew installDebug
```

Or open in Android Studio:

```bash
open . -a "Android Studio"
```

Then select the **app** module, choose a connected device or emulator, and press **▶ Run**.

---

## Project Structure

```
My Launcher/
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Project settings
├── app/
│   ├── build.gradle.kts          # App module config
│   └── src/main/
│       ├── AndroidManifest.xml   # App manifest (launcher intent)
│       ├── java/                 # Kotlin source code
│       └── res/                  # Layouts, drawables, values
└── docs/
    ├── My Launcher PRD.md       # Product Requirements Document
    ├── My Launcher User Guide.md # User guide
    ├── RELEASE-NOTES.md          # Release notes
    └── instructions/             # Workflow triggers
```

---

## License

TBD

---

## Contributing

TBD
