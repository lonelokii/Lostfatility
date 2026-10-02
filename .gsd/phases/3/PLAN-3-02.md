---
phase: 3
plan: 3-02
wave: 2
gap_closure: false
depends_on: ["3-01"]
status: complete
---

# Plan 3-02: Terminal Game Loop & Interactive Verification

## Objective
Implement CliApp as an executable interactive game runner and verify end-to-end campaign playthrough via automated headless simulation test.

## Tasks

<task type="auto">
  <name>Implement CliApp terminal game runner</name>
  <files>
    src/main/java/lostfacility/cli/CliApp.java
  </files>
  <action>
    Create CliApp providing an interactive console loop: renders ASCII map and HUD, handles directional movement (WASD), actions (attack, take, use, equip, talk, look, save, load, quit), dialogue choices, and win/lose screens.
  </action>
  <verify>
    CliApp compiles cleanly with main entry point declared in pom.xml.
  </verify>
  <done>
    CliApp fully implemented and playable.
  </done>
</task>

<task type="auto">
  <name>Implement CliCampaignPlaythroughTest</name>
  <files>
    src/test/java/lostfacility/cli/CliCampaignPlaythroughTest.java
  </files>
  <action>
    Create automated end-to-end playthrough test simulating entire game walkthrough:
    Maintenance Room -> Key & Sword -> Corridor -> Defeat Robot -> Security Room -> Talk to Dr. Aris -> Receive Access Card -> Unlock Blast Door -> Surface Airway -> Escape victory!
  </action>
  <verify>
    Run mvn test to verify full campaign playthrough passes with zero failures.
  </verify>
  <done>
    Full campaign playable end-to-end and empirically verified (19/19 tests passing).
  </done>
</task>

## Must-Haves
- [x] CliApp provides an interactive text-driven console game loop
- [x] CliCampaignPlaythroughTest proves 100% end-to-end playable victory

## Success Criteria
- [x] Maven test suite passes completely with zero errors
