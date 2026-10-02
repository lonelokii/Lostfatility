---
phase: 3
plan: 3-01
wave: 1
gap_closure: false
depends_on: []
status: complete
---

# Plan 3-01: ASCII Map Renderer & CLI Output Formatter

## Objective
Implement dedicated ASCII map viewport rendering and stylized terminal HUD output formatting for the headless CLI interface.

## Tasks

<task type="auto">
  <name>Implement AsciiMapRenderer</name>
  <files>
    src/main/java/lostfacility/cli/AsciiMapRenderer.java
  </files>
  <action>
    Create AsciiMapRenderer to render room layouts with entities and items (@, R, N, +, /, *, X, #, .) with optional ANSI colors.
  </action>
  <verify>
    Class compiles and accurately renders test room snapshots.
  </verify>
  <done>
    AsciiMapRenderer functional and clean.
  </done>
</task>

<task type="auto">
  <name>Implement CliOutputFormatter</name>
  <files>
    src/main/java/lostfacility/cli/CliOutputFormatter.java
  </files>
  <action>
    Create CliOutputFormatter managing terminal output presentation: Top stats HUD, HP bar [██████░░░░], room ASCII map, dialogue overlay box, active quest tracker, and recent log messages.
  </action>
  <verify>
    Formatting builds cleanly without null pointers.
  </verify>
  <done>
    CliOutputFormatter renders complete dashboard snapshot.
  </done>
</task>

## Must-Haves
- [x] AsciiMapRenderer accurately displays player, enemies, NPCs, items, and doors
- [x] CliOutputFormatter renders HP bar, stats, quest objectives, and message logs

## Success Criteria
- [x] Clean compilation with zero external dependencies
