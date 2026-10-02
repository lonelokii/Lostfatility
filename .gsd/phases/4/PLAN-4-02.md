---
phase: 4
plan: 4-02
wave: 2
gap_closure: false
depends_on: [4-01]
status: complete
---

# Plan 4-02: Retro HUD Layout, Controls, Dialogue Overlay, AudioService & MainApp

## Objective
Implement the complete JavaFX application layer: cyber-retro HUD layout, interactive dialogue choice overlay, modular audio service with silent fallback, comprehensive keyboard/button controls, and the `MainApp` application bootstrap.

## Tasks

<task type="auto">
  <name>Implement AudioService</name>
  <files>
    src/main/java/lostfacility/gui/AudioService.java
  </files>
  <action>
    Create AudioService providing sound effect triggers (STEP, ATTACK, HIT, DEFEAT, ITEM_PICKUP, DOOR_OPEN, DIALOGUE, VICTORY, GAME_OVER).
    Uses JavaFX AudioClip / javax.sound.sampled tone generation, with silent no-op fallback for headless or sound-disabled environments.
  </action>
  <verify>
    AudioService methods can be called safely without throwing exceptions even if audio device is unavailable.
  </verify>
  <done>
    AudioService provides sound effects with resilient silent fallback.
  </done>
</task>

<task type="auto">
  <name>Implement DialogueOverlay</name>
  <files>
    src/main/java/lostfacility/gui/DialogueOverlay.java
  </files>
  <action>
    Create DialogueOverlay as a retro cyberpunk modal box:
    - Displays speaker name, portrait/icon, and dialogue text.
    - Presents numbered choice buttons ([1]..., [2]...) clickable with mouse or selectable with keyboard number keys.
    - Sends TalkAction directly through GameEngine when an option is selected.
  </action>
  <verify>
    Overlay updates when dialogue node changes and hides cleanly when dialogue ends.
  </verify>
  <done>
    DialogueOverlay seamlessly presents branching conversations.
  </done>
</task>

<task type="auto">
  <name>Implement GameView and UI Layout</name>
  <files>
    src/main/java/lostfacility/gui/GameView.java
    src/main/resources/styles/theme.css
  </files>
  <action>
    Create GameView managing the full UI layout (BorderPane):
    - Top HUD: Room title, Area description, Player HP bar with numeric label, ATK/DEF stats, Turn counter.
    - Center: StackPane with GameCanvas and DialogueOverlay.
    - Right Panel: Inventory list with item action buttons (Use, Equip, Drop); Quest tracker with active/completed objectives.
    - Bottom Panel: Stylized retro terminal log with auto-scroll, command line text field, and quick action buttons (WASD/Arrows, Attack, Take, Talk, Save, Load, Mute).
    - Create dark cyberpunk CSS theme (neon cyan, amber, dark slate, glowing borders).
  </action>
  <verify>
    Layout binds accurately to GameState and refreshes on GameEvents.
  </verify>
  <done>
    GameView creates immersive retro sci-fi GUI.
  </done>
</task>

<task type="auto">
  <name>Implement MainApp Entry Point</name>
  <files>
    src/main/java/lostfacility/gui/MainApp.java
  </files>
  <action>
    Create MainApp extending javafx.application.Application:
    - Loads campaign world via JsonLoader.
    - Initializes GameEngine, GameView, EventManager, and AudioService.
    - Sets up Scene (1100x820), registers global keyboard shortcuts (WASD, Arrows, Space, 1-9, Enter), and displays PrimaryStage.
  </action>
  <verify>
    MainApp compiles and is configured as mainClass in javafx-maven-plugin.
  </verify>
  <done>
    MainApp bootstraps full graphical game.
  </done>
</task>

## Must-Haves
- [x] AudioService plays audio or falls back silently without errors
- [x] DialogueOverlay displays NPC dialogue and interactive choice buttons
- [x] GameView displays Top HUD, Canvas center, Inventory/Quest right sidebar, and Console/Action toolbar bottom
- [x] MainApp boots the complete JavaFX application

## Success Criteria
- [x] Clean compilation with `mvn test-compile`
- [x] All headless JUnit tests continue to pass (23/23)
