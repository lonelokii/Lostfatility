---
milestone: v1.0
version: 1.0.0
updated: 2026-10-02T07:15:00Z
---

# ROADMAP.md — Project Roadmap

> **Current Phase:** Phase 4 — JavaFX Presentation Layer  
> **Status:** 🔄 Ready to Execute  

## Must-Haves (from SPEC)

- [x] Reusable decoupled engine core with zero JavaFX dependencies (Verified in Phase 1)
- [x] Data-driven game data loading from JSON (`games/lost_facility/`) (Verified in Phase 2)
- [x] Turn-based combat, enemy AI, collision, inventory, and quest tracking (Verified in Phase 1 & 2)
- [x] Interactive playable Terminal CLI mode (`CliApp`) (Verified in Phase 3)
- [ ] JavaFX 2D graphical mode (`MainApp`) with Canvas, HUD, animations, and sound
- [ ] Complete demonstration campaign "The Lost Facility" from start to exit

---

## Phases

### Phase 1: Project Foundation & Domain Core
**Status:** ✅ Complete  
**Objective:** Set up Maven configuration, define domain models (`World`, `Room`, `Tile`, `Entity`, `Player`, `Enemy`, `Item`, `Inventory`), create `GameAction` and `GameEvent` hierarchies, implement `CommandParser`, and verify headless mechanics with JUnit 5 unit tests.  
**Requirements:** REQ-01, REQ-02, REQ-03, REQ-08, REQ-09  

**Plans:**
- [x] Plan 1-01: Maven Setup & Domain Entity Models
- [x] Plan 1-02: Command Parser, Action Dispatcher & Event Bus
- [x] Plan 1-03: Headless Engine Verification & Unit Tests (14/14 tests passing)

---

### Phase 2: Game Systems & JSON Persistence
**Status:** ✅ Complete  
**Objective:** Implement `CombatSystem` (damage formula, retaliation), `QuestManager` (objective progression), `DialogueManager` (branching choice trees), and `JsonLoader` / `SaveManager` with multi-slot persistence. Author `games/lost_facility/` content files.  
**Depends on:** Phase 1  
**Requirements:** REQ-04, REQ-05, REQ-06, REQ-07, REQ-10, REQ-11  

**Plans:**
- [x] Plan 2-01: Combat, Quest & Dialogue Systems
- [x] Plan 2-02: JSON Content Loader, Save/Load Manager & Data Files
- [x] Plan 2-03: Systems & Persistence Integration Tests (18/18 tests passing)

---

### Phase 3: Playable Terminal CLI Game Loop
**Status:** ✅ Complete  
**Objective:** Build an interactive terminal CLI game runner (`CliApp`) rendering ASCII maps, accepting text commands, printing colored combat logs, and allowing full gameplay from start to escape without any GUI.  
**Depends on:** Phase 2  
**Requirements:** REQ-12  

**Plans:**
- [x] Plan 3-01: ASCII Map Renderer & CLI Output Formatter
- [x] Plan 3-02: Terminal Game Loop & Interactive Verification (19/19 tests passing)

---

### Phase 4: JavaFX Presentation Layer
**Status:** ✅ Complete  
**Objective:** Construct the JavaFX application (`MainApp`) featuring a 60 FPS `Canvas` renderer, `AnimationTimer` with 150ms tile travel interpolation, 5-frame attack animations, a dark sci-fi retro HUD, dialogue overlay, and modular `AudioService`.  
**Depends on:** Phase 3  
**Requirements:** REQ-13, REQ-14, REQ-15  

**Plans:**
- [x] Plan 4-01: JavaFX Canvas Viewport, AnimationTimer & Sprite Engine
- [x] Plan 4-02: Retro HUD Layout, Controls, Dialogue Overlay & AudioService

---

### Phase 5: Demo Campaign Assets & End-to-End Verification
**Status:** ✅ Complete  
**Objective:** Bundle 32x32 retro pixel-art sprites and sound effects with procedural fallbacks, verify full demonstration campaign playthrough from Maintenance Room to Main Exit, and finalize documentation.  
**Depends on:** Phase 4  
**Requirements:** REQ-10, REQ-13, REQ-15  

**Plans:**
- [x] Plan 5-01: Demo Campaign Assets & Packaging
- [x] Plan 5-02: End-to-End System Verification & Final Documentation

---

## Progress Summary

| Phase | Status | Plans | Complete |
|---|---|---|---|
| **1. Foundation & Domain Core** | ✅ Complete | 3/3 | 100% |
| **2. Systems & Persistence** | ✅ Complete | 3/3 | 100% |
| **3. Playable Terminal CLI** | ✅ Complete | 2/2 | 100% |
| **4. JavaFX Presentation Layer** | ✅ Complete | 2/2 | 100% |
| **5. Assets & Verification** | ✅ Complete | 2/2 | 100% |
| **Total Project Progress** | ✅ **v1.0 Complete** | **12/12** | **100%** |
