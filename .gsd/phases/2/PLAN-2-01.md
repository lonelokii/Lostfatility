---
phase: 2
plan: 2-01
wave: 1
gap_closure: false
depends_on: []
status: complete
---

# Plan 2-01: Combat, Quest & Dialogue Systems

## Objective
Implement the formal CombatSystem, the branching Dialogue subsystem, and the reactive QuestManager tracking objectives via GameEvents.

## Tasks

<task type="auto">
  <name>Implement CombatSystem and TalkAction</name>
  <files>
    src/main/java/lostfacility/system/CombatSystem.java
    src/main/java/lostfacility/action/TalkAction.java
  </files>
  <action>
    Implement CombatSystem with damage formulas and retaliation; implement TalkAction to initiate NPC conversations.
  </action>
  <verify>
    Files compile and integrate with GameAction and Entity models.
  </verify>
  <done>
    CombatSystem and TalkAction implemented.
  </done>
</task>

<task type="auto">
  <name>Implement DialogueManager and QuestManager</name>
  <files>
    src/main/java/lostfacility/system/DialogueNode.java
    src/main/java/lostfacility/system/DialogueChoice.java
    src/main/java/lostfacility/system/DialogueManager.java
    src/main/java/lostfacility/system/QuestState.java
    src/main/java/lostfacility/system/QuestObjective.java
    src/main/java/lostfacility/system/Quest.java
    src/main/java/lostfacility/system/QuestManager.java
  </files>
  <action>
    Implement DialogueNode, DialogueChoice, DialogueManager with branching choices, and QuestManager listening to GameEvents.
  </action>
  <verify>
    Branching choices and quest event triggers compile and test cleanly.
  </verify>
  <done>
    Dialogue and Quest subsystems fully implemented and decoupled.
  </done>
</task>

## Must-Haves
- [x] CombatSystem centralizes combat math and retaliation
- [x] DialogueManager supports multi-choice branching conversations
- [x] QuestManager automatically reacts to GameEvents to progress quest objectives

## Success Criteria
- [x] Clean compilation with zero external errors
- [x] Zero JavaFX imports in systems package
