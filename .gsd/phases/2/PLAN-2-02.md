---
phase: 2
plan: 2-02
wave: 2
gap_closure: false
depends_on: ["2-01"]
status: complete
---

# Plan 2-02: JSON Content Loader, Save/Load Manager & Data Files

## Objective
Implement Jackson-based JsonLoader and multi-slot SaveManager, and author the external campaign data files for The Lost Facility.

## Tasks

<task type="auto">
  <name>Implement JsonLoader and SaveManager</name>
  <files>
    src/main/java/lostfacility/persistence/JsonLoader.java
    src/main/java/lostfacility/persistence/SaveManager.java
    src/main/java/lostfacility/persistence/SaveMetadata.java
    src/main/java/lostfacility/action/SaveAction.java
    src/main/java/lostfacility/action/LoadAction.java
  </files>
  <action>
    Implement JsonLoader parsing campaign JSON files; implement multi-slot SaveManager persisting GameState to JSON; implement SaveAction and LoadAction.
  </action>
  <verify>
    JSON serialization and deserialization classes compile and handle IO cleanly.
  </verify>
  <done>
    JsonLoader and SaveManager implemented and integrated with GameAction pipeline.
  </done>
</task>

<task type="auto">
  <name>Author The Lost Facility campaign JSON data files</name>
  <files>
    src/main/resources/games/lost_facility/world.json
    src/main/resources/games/lost_facility/items.json
    src/main/resources/games/lost_facility/enemies.json
    src/main/resources/games/lost_facility/npcs.json
    src/main/resources/games/lost_facility/dialogue.json
    src/main/resources/games/lost_facility/quests.json
  </files>
  <action>
    Create all external JSON data files for the reference campaign: world.json (5 rooms), items.json (6 items), enemies.json (2 robots), npcs.json (Dr. Aris), dialogue.json (branching scientist tree), quests.json (Escape the Facility).
  </action>
  <verify>
    Verify JSON syntax validity for all files.
  </verify>
  <done>
    Complete game campaign authored as data-driven JSON.
  </done>
</task>

## Must-Haves
- [x] JsonLoader parses external campaign content without hardcoding in Java
- [x] SaveManager persists full GameState to JSON files with metadata
- [x] Campaign data provides a complete 5-room adventure from start to escape

## Success Criteria
- [x] Clean JSON syntax across all authored data files
- [x] Engine successfully loads world and entities from files
