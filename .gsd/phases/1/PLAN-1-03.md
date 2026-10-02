---
phase: 1
plan: 1-03
wave: 3
gap_closure: false
depends_on: ["1-02"]
status: complete
---

# Plan 1-03: Headless Engine Verification & Unit Tests

## Objective
Write comprehensive JUnit 5 tests covering grid collision, movement, combat math, inventory operations, and command parsing, verifying headless correctness and establishing empirical proof.

## Context
- .gsd/phases/1/PLAN-1-01.md
- .gsd/phases/1/PLAN-1-02.md
- .gsd/REQUIREMENTS.md
- .gsd/SPEC.md

## Tasks

<task type="auto">
  <name>Implement JUnit 5 unit test suites for headless engine mechanics</name>
  <files>
    src/test/java/lostfacility/engine/MovementAndCollisionTest.java
    src/test/java/lostfacility/engine/InventoryAndItemTest.java
    src/test/java/lostfacility/engine/CombatMathTest.java
    src/test/java/lostfacility/engine/CommandParserTest.java
  </files>
  <action>
    Create JUnit 5 tests testing movement, collision, locked doors, inventory limits, potions, equipment attack bonuses, combat damage formula, enemy retaliation/defeat, and command parsing.
  </action>
  <verify>
    All test files created in src/test/java/lostfacility/engine/.
  </verify>
  <done>
    Unit tests written covering movement, collision, combat, inventory, and parser.
  </done>
</task>

<task type="auto">
  <name>Compile and execute headless test suite</name>
  <files>
    src/test/java/lostfacility/engine/MovementAndCollisionTest.java
    src/test/java/lostfacility/engine/InventoryAndItemTest.java
    src/test/java/lostfacility/engine/CombatMathTest.java
    src/test/java/lostfacility/engine/CommandParserTest.java
  </files>
  <action>
    Run mvn test to execute all tests headlessly.
  </action>
  <verify>
    mvn test passes with 0 failures and 0 errors.
  </verify>
  <done>
    14 unit tests pass cleanly with 0 failures, 0 errors, validating Phase 1 requirements.
  </done>
</task>

## Must-Haves
- [x] Collision blocks walls and doors deterministically
- [x] Inventory and item usage operate correctly
- [x] Combat math obeys PRD formulas and handles retaliations
- [x] CommandParser correctly decodes natural text inputs

## Success Criteria
- [x] All unit tests pass with zero failures (14/14 tests passing)
- [x] Zero JavaFX runtime dependencies needed to run tests
