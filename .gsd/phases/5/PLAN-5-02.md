---
phase: 5
plan: 5-02
wave: 2
gap_closure: false
depends_on: [5-01]
status: complete
---

# Plan 5-02: End-to-End System Verification & Final Documentation

## Objective
Verify the end-to-end campaign walkthrough via comprehensive integration tests, validate full game loop (combat, dialogues, quests, room transitions, save/load, victory escape condition), and write complete developer and user documentation in `README.md`.

## Tasks

<task type="auto">
  <name>Implement FullCampaignIntegrationTest</name>
  <files>
    src/test/java/lostfacility/engine/FullCampaignIntegrationTest.java
  </files>
  <action>
    Create FullCampaignIntegrationTest simulating complete canonical playthrough of "The Lost Facility" campaign:
    - Waking up in Maintenance Room
    - Gathering items, equipping weapons, unlocking doors
    - Defeating security robots with combat retaliation
    - Talking to Dr. Sarah Chen with branching choices
    - Collecting Level 3 Access Card and advancing quests
    - Overcoming final obstacles and escaping through the Main Exit
    - Verifying save/load persistence mid-campaign
  </action>
  <verify>
    Test executes headlessly and asserts game victory condition with zero errors.
  </verify>
  <done>
    Full campaign flow verified from start to victory.
  </done>
</task>

<task type="auto">
  <name>Author Comprehensive README and Documentation</name>
  <files>
    README.md
  </files>
  <action>
    Write complete, professional project README.md:
    - Architecture & Design Patterns (Engine decoupling, Command Pattern, Event Bus, MVC)
    - System Requirements & Quick Start (GUI mode, CLI mode)
    - Game Controls & Commands Reference
    - JSON Campaign Authoring Guide (World layouts, items, enemies, dialogues, quests)
    - Project Structure and verification summary
  </action>
  <verify>
    README.md is well-structured, links to source files, and contains accurate CLI/GUI execution instructions.
  </verify>
  <done>
    Comprehensive documentation available for players, developers, and educators.
  </done>
</task>

## Must-Haves
- [x] FullCampaignIntegrationTest verifies end-to-end victory condition headlessly
- [x] README.md contains complete architecture, execution instructions, controls, and authoring guide

## Success Criteria
- [x] All JUnit 5 tests pass (`mvn test`)
- [x] Documentation complete and verified
