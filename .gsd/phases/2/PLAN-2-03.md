---
phase: 2
plan: 2-03
wave: 3
gap_closure: false
depends_on: ["2-02"]
status: complete
---

# Plan 2-03: Systems & Persistence Integration Tests

## Objective
Implement comprehensive integration tests verifying JSON data loading, dialogue branching, quest event triggers, and save/load state persistence.

## Tasks

<task type="auto">
  <name>Implement integration test suites for Phase 2 systems</name>
  <files>
    src/test/java/lostfacility/system/QuestAndDialogueTest.java
    src/test/java/lostfacility/persistence/JsonLoaderTest.java
    src/test/java/lostfacility/persistence/SaveManagerTest.java
  </files>
  <action>
    Create JUnit 5 tests covering campaign JSON loading, branching dialogue choices, quest events, and save/load roundtrips.
  </action>
  <verify>
    All test files created and compile cleanly.
  </verify>
  <done>
    Integration tests covering all Phase 2 systems implemented.
  </done>
</task>

<task type="auto">
  <name>Execute headless test suite via Maven</name>
  <files>
    src/test/java/lostfacility/system/QuestAndDialogueTest.java
    src/test/java/lostfacility/persistence/JsonLoaderTest.java
    src/test/java/lostfacility/persistence/SaveManagerTest.java
  </files>
  <action>
    Run `mvn test` to execute all 18 tests across Phase 1 and Phase 2.
  </action>
  <verify>
    mvn test passes with 0 failures and 0 errors.
  </verify>
  <done>
    18 unit and integration tests passing cleanly with 100% success rate.
  </done>
</task>

## Must-Haves
- [x] JsonLoaderTest confirms external campaign JSON loads into World and Entities
- [x] QuestAndDialogueTest confirms dialogue and quest progression
- [x] SaveManagerTest proves exact state persistence round-trip

## Success Criteria
- [x] Zero failures across all unit and integration tests (18/18 tests passing)
- [x] Phase 2 requirements REQ-04, REQ-05, REQ-06, REQ-07, REQ-10, REQ-11 fully satisfied
