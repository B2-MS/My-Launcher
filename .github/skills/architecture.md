---
description: Architecture guidance for My Launcher (Android metro-style home screen replacement)
applyTo: "**/*.{kt,kts,xml}"
---

# Architecture Skills - My Launcher

## Architecture Principles

- Keep features modular with clear boundaries between UI, domain, and data layers.
- Prefer unidirectional data flow and immutable view state where practical.
- Isolate platform services behind interfaces to keep core logic testable.
- Keep persistence and external integrations decoupled from presentation code.

## Platform Context

- Product domain: Android metro-style home screen replacement
- Platform: Android 10+
- Prioritize responsiveness, reliability, and clear error handling.

## Code Organization

- UI layer: rendering, input handling, and view-level state only.
- Domain layer: business rules, orchestration, and policies.
- Data layer: storage, network, file I/O, and adapters.

## Change Guidance

- Favor small, composable components over large monolith files.
- Add extension points for growth without over-engineering.
- Document important design decisions in concise ADRs.
