---
phase: 1
plan: 1-01
wave: 1
gap_closure: false
depends_on: []
status: complete
---

# Plan 1-01: Maven Setup & Core World & Item Domain Models

## Objective
Establish the Maven project foundation with Java 17+, JavaFX, Jackson, and JUnit 5, and implement pure Java domain models for tiles, coordinates, rooms, world container, items, and inventory without any GUI dependencies.

## Context
- .gsd/SPEC.md
- .gsd/REQUIREMENTS.md
- .gsd/DECISIONS.md
- ARCHITECTURE.md

## Tasks

<task type="auto">
  <name>Configure Maven pom.xml and directory structure</name>
  <files>
    pom.xml
  </files>
  <action>
    Create pom.xml configured for Java 17 Standard Edition with JavaFX 21, Jackson Databind, and JUnit 5.
  </action>
  <verify>
    mvn compile succeeds.
  </verify>
  <done>
    pom.xml exists with JavaFX 21, Jackson Databind, and JUnit 5 dependencies declared cleanly.
  </done>
</task>

<task type="auto">
  <name>Implement World, Tile, and Item domain models</name>
  <files>
    src/main/java/lostfacility/model/Position.java
    src/main/java/lostfacility/model/Direction.java
    src/main/java/lostfacility/model/TileType.java
    src/main/java/lostfacility/model/Tile.java
    src/main/java/lostfacility/model/Room.java
    src/main/java/lostfacility/model/World.java
    src/main/java/lostfacility/model/ItemType.java
    src/main/java/lostfacility/model/Item.java
    src/main/java/lostfacility/model/Inventory.java
  </files>
  <action>
    Implement clean domain classes in lostfacility.model with zero JavaFX imports.
  </action>
  <verify>
    All classes compile and support ASCII grid parsing.
  </verify>
  <done>
    World, Room, Tile, Item, and Inventory classes compiled and ready for entity and action wiring.
  </done>
</task>

## Must-Haves
- [x] pom.xml configured for Java 17, JavaFX, Jackson, and JUnit 5
- [x] Pure Java domain classes created with zero GUI dependencies
- [x] ASCII string parsing supported on Room to generate tile maps

## Success Criteria
- [x] All tasks completed without errors
- [x] No javafx.* imports in domain models
- [x] Code follows standard Java conventions
