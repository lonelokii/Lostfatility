---
milestone: v1.0
version: 1.0.0
updated: 2026-10-02T07:00:00Z
---

# ROADMAP.md — Project Roadmap

> **Current Phase:** Phase 2 — Game Systems & JSON Persistence  
> **Status:** 🔄 Planning Phase 2  

## Must-Haves (from SPEC)

- [x] Reusable decoupled engine core with zero JavaFX dependencies (Verified in Phase 1)
- [ ] Data-driven game data loading from JSON (`games/lost_facility/`)
- [ ] Turn-based combat, enemy AI, collision, inventory, and quest tracking
- [ ] Interactive playable Terminal CLI mode (`CliApp`)
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
**Status:** 🔄 Ready to Execute  
**Objective:** Implement `CombatSystem` (damage formula, retaliation), `QuestManager` (objective progression), `DialogueManager` (branching choice trees), and `JsonLoader` / `SaveManager` with multi-slot persistence. Author `games/lost_facility/` content files.  
**Depends on:** Phase 1  
**Requirements:** REQ-04, REQ-05, REQ-06, REQ-07, REQ-10, REQ-11  

---

### Phase 3: Playable Terminal CLI Game Loop
**Status:** ⬜ Not Started  
**Objective:** Build an interactive terminal CLI game runner (`CliApp`) rendering ASCII maps, accepting text commands, printing colored combat logs, and allowing full gameplay from start to escape without any GUI.  
**Depends on:** Phase 2  
**Requirements:** REQ-12  

---

### Phase 4: JavaFX Presentation Layer
**Status:** ⬜ Not Started  
**Objective:** Construct the JavaFX application (`MainApp`) featuring a 60 FPS `Canvas` renderer, `AnimationTimer` with 150ms tile travel interpolation, 5-frame attack animations, a dark sci-fi retro HUD, dialogue overlay, and modular `AudioService`.  
**Depends on:** Phase 3  
**Requirements:** REQ-13, REQ-14, REQ-15  

---

### Phase 5: Demo Campaign Assets & End-to-End Verification
**Status:** ⬜ Not Started  
**Objective:** Bundle 32x32 retro pixel-art sprites and sound effects with procedural fallbacks, verify full demonstration campaign playthrough from Maintenance Room to Main Exit, and finalize documentation.  
**Depends on:** Phase 4  
**Requirements:** REQ-10, REQ-13, REQ-15  

---

## Progress Summary

| Phase | Status | Plans | Complete |
|---|---|---|---|
| **1. Foundation & Domain Core** | ✅ Complete | 3/3 | 100% |
| **2. Systems & Persistence** | 🔄 Ready | 0/3 | 0% |
| **3. Playable Terminal CLI** | ⬜ Not Started | 0/2 | 0% |
| **4. JavaFX Presentation Layer** | ⬜ Not Started | 0/2 | 0% |
| **5. Assets & Verification** | ⬜ Not Started | 0/2 | 0% |
