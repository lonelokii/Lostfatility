# SPEC.md — Project Specification

> **Status**: `FINALIZED`
>
> ⚠️ **Planning Lock**: Requirements defined and finalized based on PRD, Game Specification, and Architecture alignment.

---

## Vision

A reusable 2D GUI-based text-driven adventure and RPG engine written in Java 17+ and JavaFX, demonstrated by **The Lost Facility** — a retro mystery adventure game where the player explores an abandoned research facility, interacts with NPCs, battles rogue security robots, and escapes. The engine is strictly decoupled from game content and presentation, supporting headless testing and data-driven game authoring via JSON.

---

## Goals

1. **Reusable Decoupled Engine Core** — A pure Java SE domain layer (world, entities, combat, inventory, dialogue, quests, events) completely independent of JavaFX or terminal graphics.
2. **Data-Driven Game Authoring** — All game content (rooms, ASCII tile layouts, enemies, items, NPCs, dialogue, quests) loaded from human-readable JSON files (`games/lost_facility/`).
3. **Unified Dual Interaction (CLI & GUI)** — Full support for both a terminal CLI loop and a JavaFX graphical interface (with a 2D Canvas viewport, smooth 150ms tile interpolation, attack animations, and retro HUD) through a unified Command Pattern (`GameAction`).
4. **Resilient Persistence & Systems** — Multi-slot JSON save/load with auto-save, robust collision, turn-based enemy AI, branching dialogue, and modular audio with silent fallback.

---

## Non-Goals (Out of Scope)

- **3D Graphics & Physics Engines** — Strictly 2D grid/tile-based logic.
- **Real-Time Multiplayer & Networking** — Strictly local single-player experience.
- **Complex Enemy Pathfinding / Machine Learning** — Simple state-based behavior (Idle, Detect, Chase, Attack, Defeated).
- **External Map Editor Software** — Human-readable ASCII grids inside JSON files rather than proprietary TMX/Tiled dependencies.
- **Voice Acting & Large Pre-Rendered Cinematics** — Focus on text logs, dialogue overlays, and pixel animations.

---

## Users

- **Computer Science & Game Dev Students:** Learning clean OOP design, design patterns (Command, Observer, MVC/Presenter), separation of concerns, and JavaFX graphics.
- **Text Adventure / RPG Players:** Enjoying a short 15–30 minute retro sci-fi mystery through keyboard hotkeys, GUI buttons, and typed command input.

---

## Constraints

- **Language & Runtime:** Java 17 or higher.
- **Build System:** Apache Maven (`pom.xml`) with `javafx-maven-plugin`.
- **GUI Framework:** JavaFX 21 (Canvas for world rendering, standard controls for HUD).
- **JSON Serialization:** Jackson Databind 2.17+.
- **Zero Internet Requirement:** The engine and demonstration game must run completely offline without remote network dependencies.
- **Headless Testability:** 100% of game mechanics, combat formulas, movement rules, quests, and persistence must be testable via JUnit 5 without opening a JavaFX Stage.

---

## Success Criteria

- [ ] Maven build compiles cleanly with zero warnings or external errors.
- [ ] Comprehensive JUnit 5 unit tests pass headlessly, covering collision, movement, combat math, inventory, quest transitions, and save/load cycles.
- [ ] Terminal CLI mode (`CliApp`) is fully playable from start to escape using text commands and ASCII map rendering.
- [ ] JavaFX GUI mode (`MainApp`) launches with 60 FPS Canvas rendering, smooth tile movement interpolation, 5-frame attack animations, responsive HUD, and dialogue overlay.
- [ ] Demonstration campaign "The Lost Facility" is fully playable from the Maintenance Room to the Main Exit win condition.
- [ ] Save game files accurately serialize full game state and reload identically without data loss.

---

*Last updated: 2026-10-02*
