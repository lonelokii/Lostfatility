---
phase: 4
plan: 4-01
wave: 1
gap_closure: false
depends_on: []
status: complete
---

# Plan 4-01: JavaFX Canvas Viewport, AnimationTimer & Sprite Engine

## Objective
Implement the 2D rendering foundation for the JavaFX presentation layer in `lostfacility.gui`: high-fidelity procedural and sprite tile rendering, 60 FPS `AnimationTimer` game loop, smooth 150ms tile position interpolation, and 5-frame attack/hit animations with floating damage indicators.

## Tasks

<task type="auto">
  <name>Implement SpriteManager</name>
  <files>
    src/main/java/lostfacility/gui/SpriteManager.java
  </files>
  <action>
    Create SpriteManager providing rendering routines for tiles (wall, floor, door, exit), items (keycard, medkit, weapon, battery), player sprite with directional facing, enemies (patrol bot, drone, turret), and NPCs. Supports loading PNG assets with rich procedural Canvas vector drawing fallbacks.
  </action>
  <verify>
    Compiles cleanly and renders distinctive visual representations for all entity and tile types.
  </verify>
  <done>
    SpriteManager provides tile and entity rendering routines.
  </done>
</task>

<task type="auto">
  <name>Implement AnimationController and Visual FX</name>
  <files>
    src/main/java/lostfacility/gui/AnimationController.java
    src/main/java/lostfacility/gui/FloatingText.java
  </files>
  <action>
    Create AnimationController running on JavaFX AnimationTimer at 60 FPS:
    - Interpolates entity movement between previous and current tile coordinates over 150ms.
    - Manages combat visual effects: 5-frame attack swing/sparks, screen shake (offset +/- 3px on hit), and floating damage numbers fading upwards.
  </action>
  <verify>
    AnimationController updates delta times, updates lerp ratios [0.0..1.0], and advances active effects without memory leaks.
  </verify>
  <done>
    Smooth interpolation and visual effects pipeline operational.
  </done>
</task>

<task type="auto">
  <name>Implement GameCanvas</name>
  <files>
    src/main/java/lostfacility/gui/GameCanvas.java
  </files>
  <action>
    Create GameCanvas extending javafx.scene.canvas.Canvas:
    - Renders current room tiles, exits, doors, items, NPCs, enemies, and player.
    - Renders entity health bars and active status badges.
    - Renders active combat animations and floating damage numbers.
    - Subscribes to GameEvent bus (MoveEvent, CombatEvent, ItemEvent) to automatically trigger animations.
  </action>
  <verify>
    Canvas renders complete room state cleanly; unit tests verify event subscriptions.
  </verify>
  <done>
    GameCanvas provides interactive graphical rendering of game world.
  </done>
</task>

## Must-Haves
- [x] SpriteManager renders walls, floors, doors, player, enemies, NPCs, and items
- [x] AnimationController provides 150ms movement interpolation and attack FX
- [x] GameCanvas renders room, entities, health bars, and subscribes to GameEvents

## Success Criteria
- [x] Clean compilation with `mvn test-compile`
