---
phase: 1
plan: 1-02
wave: 2
gap_closure: false
depends_on: ["1-01"]
status: complete
---

# Plan 1-02: Entities, Unified Actions, and Reactive Event Bus

## Objective
Implement entity models (Player, Enemy), the strongly typed GameEvent hierarchy, the unified GameAction command pattern, and the central GameEngine action execution loop with CommandParser.

## Context
- .gsd/phases/1/PLAN-1-01.md
- .gsd/SPEC.md
- .gsd/REQUIREMENTS.md
- ARCHITECTURE.md

## Tasks

<task type="auto">
  <name>Implement Entity classes and reactive GameEvent hierarchy</name>
  <files>
    src/main/java/lostfacility/model/Entity.java
    src/main/java/lostfacility/model/Player.java
    src/main/java/lostfacility/model/Enemy.java
    src/main/java/lostfacility/model/EnemyState.java
    src/main/java/lostfacility/event/GameEvent.java
    src/main/java/lostfacility/event/MoveEvent.java
    src/main/java/lostfacility/event/CombatEvent.java
    src/main/java/lostfacility/event/ItemEvent.java
    src/main/java/lostfacility/event/MessageEvent.java
    src/main/java/lostfacility/event/EventManager.java
  </files>
  <action>
    Implement Entity, Player, Enemy, and thread-safe EventManager with strongly typed events.
  </action>
  <verify>
    Files compile and events publish to listeners cleanly.
  </verify>
  <done>
    Entities and event bus classes implemented in pure Java.
  </done>
</task>

<task type="auto">
  <name>Implement GameAction hierarchy, CommandParser, and GameEngine action dispatcher</name>
  <files>
    src/main/java/lostfacility/action/GameAction.java
    src/main/java/lostfacility/action/MoveAction.java
    src/main/java/lostfacility/action/AttackAction.java
    src/main/java/lostfacility/action/TakeAction.java
    src/main/java/lostfacility/action/UseAction.java
    src/main/java/lostfacility/action/EquipAction.java
    src/main/java/lostfacility/action/ExamineAction.java
    src/main/java/lostfacility/action/ActionResult.java
    src/main/java/lostfacility/engine/CommandParser.java
    src/main/java/lostfacility/engine/GameState.java
    src/main/java/lostfacility/engine/GameEngine.java
  </files>
  <action>
    Implement Command pattern actions, natural language CommandParser, and GameEngine loop with enemy turn updates.
  </action>
  <verify>
    CommandParser parses text and GameEngine executes actions deterministically.
  </verify>
  <done>
    Unified command actions, parser, and engine action execution fully functional.
  </done>
</task>

## Must-Haves
- [x] CommandParser correctly interprets natural text commands into strongly typed GameActions
- [x] GameEngine executes actions and produces ActionResults containing emitted GameEvents
- [x] Combat damage calculation complies with formula max(1, atk - def) + rand(-3, 3)

## Success Criteria
- [x] All tasks completed without errors
- [x] Engine logic decoupled from UI and headless runnable
