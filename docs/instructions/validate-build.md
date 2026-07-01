# Build Validation Workflow

**Purpose:** Automated validation script that MUST be run after every code change before reporting completion to the user.

---

## When to Run

- **After every code edit** — before telling the user the change is done
- **After fixing errors** — re-run to confirm the fix
- **Before deploying** — always validate before `installDebug`

---

## Validation Steps

### Step 1: Clean Compile Check

```bash
cd "/Users/barticus/Library/CloudStorage/OneDrive-Personal/VS Code/MacBook M1/My Launcher/app" && ./gradlew compileDebugKotlin 2>&1 | tail -30
```

**Pass criteria:** `BUILD SUCCESSFUL` with zero errors.  
**On failure:** Fix all compile errors before proceeding.

### Step 2: Lint Check

```bash
cd "/Users/barticus/Library/CloudStorage/OneDrive-Personal/VS Code/MacBook M1/My Launcher/app" && ./gradlew lintDebug 2>&1 | tail -30
```

**Pass criteria:** `BUILD SUCCESSFUL` with zero errors (warnings are acceptable).  
**On failure:** Fix all lint errors. Warnings for dependency versions, icon shapes, and obsolete SDK folders are acceptable.

### Step 3: Install to Emulator

```bash
cd "/Users/barticus/Library/CloudStorage/OneDrive-Personal/VS Code/MacBook M1/My Launcher/app" && ./gradlew installDebug 2>&1 | tail -10
```

**Pass criteria:** `Installed on 1 device` and `BUILD SUCCESSFUL`.  
**On failure:** Check that the emulator is running. Start with:
```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb devices
```

### Step 4: Launch App

```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb shell am start -n com.mylauncher/.ui.MainActivity
```

**Pass criteria:** `Starting: Intent { cmp=com.mylauncher/.ui.MainActivity }`

---

## Quick One-Liner (All Steps)

Run compile + lint + install + launch in sequence, stopping on first failure:

```bash
cd "/Users/barticus/Library/CloudStorage/OneDrive-Personal/VS Code/MacBook M1/My Launcher/app" \
  && ./gradlew compileDebugKotlin 2>&1 | tail -5 \
  && ./gradlew lintDebug 2>&1 | tail -5 \
  && ./gradlew installDebug 2>&1 | tail -5 \
  && /opt/homebrew/share/android-commandlinetools/platform-tools/adb shell am start -n com.mylauncher/.ui.MainActivity
```

---

## Acceptable Warnings (Do Not Fix)

These lint warnings are known and acceptable:

| Warning | Reason |
|---------|--------|
| `GradleDependency` | Dependency version upgrades — intentionally pinned |
| `IconLauncherShape` | Placeholder icons — will be replaced later |
| `MonochromeLauncherIcon` | Placeholder icons — will be replaced later |
| `ObsoleteSdkInt` | Resource folder naming — cosmetic only |
| `DiscouragedApi` | `screenOrientation="unspecified"` is required for launcher |

---

## Error Categories That MUST Be Fixed

| Category | Example |
|----------|---------|
| Compile errors | `Unresolved reference`, `Type mismatch`, missing imports |
| Lint errors (not warnings) | `MissingSuperCall`, `NewApi` without version check |
| Missing parameters | Changed function signatures not updated at all call sites |
| Unused imports causing ambiguity | Conflicting star imports |

---

## Validation Report Format

After running validation, report results as:

```
✅ Compile: PASS
✅ Lint: PASS (0 errors, N warnings)
✅ Install: PASS
✅ Launch: PASS
```

Or on failure:

```
❌ Compile: FAIL — [error summary]
   → Fix applied: [description]
   → Re-running validation...
```
