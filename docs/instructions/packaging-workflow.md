# Packaging Workflow (Detailed)

Detailed packaging steps for My Launcher.

---

## Pre-Build

- [ ] All code changes committed
- [ ] Version number updated in `README.md`, `app/build.gradle.kts`, `RELEASE-NOTES.md`
- [ ] No compiler errors or warnings

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Verify

- [ ] App launches without crashes
- [ ] Core features functional
- [ ] No console errors

## Package

- [ ] App bundle located at expected path
- [ ] Documentation updated
- [ ] Git committed and pushed
