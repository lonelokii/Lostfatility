---
phase: 5
plan: 5-01
wave: 1
gap_closure: false
depends_on: []
status: complete
---

# Plan 5-01: Demo Campaign Assets & Packaging

## Objective
Bundle retro 32x32 pixel-art sprite assets into `src/main/resources/sprites/` to seamlessly integrate with `SpriteManager`, configure launcher scripts for both CLI and GUI modes, and verify Maven jar packaging.

## Tasks

<task type="auto">
  <name>Generate and Bundle Pixel Art Sprites</name>
  <files>
    src/main/resources/sprites/
  </files>
  <action>
    Generate crisp retro 32x32 pixel-art PNG sprite sheets and textures for game tiles and actors:
    - `wall.png`, `floor.png`, `door_closed.png`, `door_open.png`, `exit.png`
    - `player.png`, `robot.png`, `drone.png`, `npc.png`
    - `keycard.png`, `medkit.png`, `weapon.png`, `battery.png`
    Placed in `src/main/resources/sprites/` for classpath loading by SpriteManager.
  </action>
  <verify>
    Files exist in `target/classes/sprites/` upon compile and can be loaded via ClassLoader.
  </verify>
  <done>
    Assets loaded by SpriteManager and rendered in GUI mode.
  </done>
</task>

<task type="auto">
  <name>Configure Launcher Scripts and Build Artifacts</name>
  <files>
    scripts/run-gui.sh
    scripts/run-cli.sh
    pom.xml
  </files>
  <action>
    Create executable shell scripts `scripts/run-gui.sh` and `scripts/run-cli.sh` with proper executable permissions and argument forwarding.
    Verify Maven build and package configurations.
  </action>
  <verify>
    Scripts are executable (`chmod +x`) and launch respective targets cleanly.
  </verify>
  <done>
    Both interactive modes launch easily with single-line commands.
  </done>
</task>

## Must-Haves
- [x] 32x32 PNG sprite assets bundled in `src/main/resources/sprites/`
- [x] Launcher scripts `scripts/run-gui.sh` and `scripts/run-cli.sh` created and executable
- [x] Maven build packages jar cleanly

## Success Criteria
- [x] `mvn package -DskipTests` succeeds and produces target jar
