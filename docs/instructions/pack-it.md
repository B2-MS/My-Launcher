# Packaging Workflow

Build and package My Launcher for distribution.

---

## Step 1: Build the App

```bash
# Android release build
./gradlew assembleRelease
```

---

## Step 2: Test the Build

1. Launch the built app
2. Run through the core test checklist:

- [ ] App launches and displays the tile grid
- [ ] Tiles launch correct apps on tap
- [ ] Edit mode: long-press, drag & drop, resize, unpin all work
- [ ] Tile groups: create, expand/collapse, rename, ungroup
- [ ] App List: search, launch, long-press pin
- [ ] Settings: accent color, transparency, bevel, dark mode, themes

---

## Step 3: Update Product Documentation

Ask Copilot to update these docs to match the packaged build:

| Document | What to Update |
|----------|----------------|
| `README.md` | Version badge, feature list, build instructions |
| `docs/RELEASE-NOTES.md` | Finalize current version entry — features, fixes, known issues |
| `docs/My Launcher User Guide.md` | Ensure all user-facing instructions are accurate |

```
Update README.md, docs/RELEASE-NOTES.md, and docs/My Launcher User Guide.md for the vX.X.X package.
```

---

---

## Checklist

- [ ] App builds successfully for Android
- [ ] Core functionality verified
- [ ] `README.md` version and features updated
- [ ] `docs/RELEASE-NOTES.md` updated with new version
- [ ] `docs/My Launcher User Guide.md` updated to match current build
