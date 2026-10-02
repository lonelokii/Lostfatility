# The Lost Facility Engine

> **A reusable, decoupled 2D graphical & terminal adventure/RPG game engine built in Java 17+ and JavaFX 21.**

---

## 1. Project Overview

**The Lost Facility Engine** is a modern, modular game engine engineered with clean software architecture principles. It demonstrates how a rich, data-driven 2D adventure game can be completely decoupled from its presentation layer, enabling **100% headless testability** and seamless dual-mode execution:
- **Graphical Mode (JavaFX 21):** A cyberpunk retro GUI featuring a 60 FPS `Canvas` viewport, 150ms smooth tile movement interpolation, 5-frame combat animations, floating combat text, screen shake, branching dialogue overlays, and retro synthesized audio.
- **Terminal CLI Mode:** An interactive command-line experience rendering ANSI colored ASCII maps, health bars, inventory summaries, and real-time combat logs directly in standard terminals.

The engine is demonstrated by **"The Lost Facility"** — a tactical mystery sci-fi RPG campaign where the player awakens in an abandoned underground research complex, battles rogue security droids, assists stranded scientists, unlocks sealed blast doors, and engineers an escape to the surface.

---

## 2. Core Architecture & Design Patterns

The engine strictly follows domain-driven design, ensuring that core packages (`model`, `engine`, `system`, `persistence`) have **zero dependencies** on `javafx.*` or terminal rendering logic.

```
                      ┌────────────────────────────────────────┐
                      │             Game Content               │
                      │   (games/lost_facility/*.json files)   │
                      └───────────────────┬────────────────────┘
                                          │ JsonLoader
                                          ▼
                      ┌────────────────────────────────────────┐
                      │            Engine Core (POJO)          │
                      │  • GameState    • World & Rooms        │
                      │  • Player       • Enemies & AI         │
                      │  • Inventory    • CombatSystem         │
                      │  • QuestManager • DialogueManager      │
                      └──────────────┬──────────────────┬──────┘
                                     │                  │
               GameAction Hierarchy  │                  │  GameEvent Stream
             (Command Pattern)       │                  │  (Observer Pattern)
                                     ▼                  ▼
    ┌───────────────────────────────────┐    ┌───────────────────────────────────┐
    │       Terminal CLI Interface      │    │        JavaFX GUI Interface       │
    │  • CliApp (Interactive REPL)      │    │  • MainApp & GameView             │
    │  • AsciiMapRenderer               │    │  • GameCanvas (60 FPS Canvas)     │
    │  • CliOutputFormatter             │    │  • AnimationController (150ms)    │
    │  • ANSI Color Output              │    │  • DialogueOverlay & AudioService │
    └───────────────────────────────────┘    └───────────────────────────────────┘
```

### Key Architectural Patterns
1. **Command Pattern (`GameAction` Hierarchy):** All player actions (`MoveAction`, `AttackAction`, `TakeAction`, `UseAction`, `EquipAction`, `TalkAction`, `ExamineAction`, `SaveAction`, `LoadAction`) are encapsulated as immutable command objects executed against `GameState`.
2. **Observer Pattern (`GameEvent` Bus):** Strongly typed domain events (`MoveEvent`, `CombatEvent`, `ItemEvent`, `MessageEvent`) are published by `GameEngine` and subscribed to by presentation renderers, HUD widgets, loggers, and audio players.
3. **Data-Driven Content Authoring:** All game elements (rooms, ASCII floor layouts, enemies, stats, items, branching dialogues, quest objectives) reside in external JSON files loaded at runtime via Jackson Databind.
4. **Resilient Silent Fallback:** The modular audio engine synthesizes retro 8-bit PCM tones and gracefully degrades to silent execution in headless test or audio-disabled environments.

---

## 3. Installation & Build

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher (Eclipse Temurin, Oracle JDK, OpenJDK, or Amazon Corretto).
- **Apache Maven:** Version 3.8 or higher (*Optional*: pre-configured `./mvnw` and `mvnw.cmd` wrappers are included in the repository, so installing Maven separately is not required).

---

### Step 1: Clone the Repository

Open your terminal or command prompt:
```bash
git clone https://github.com/lonelokii/Lostfatility.git
cd Lostfatility
```

---

### Windows Setup, Compilation & Build

#### A. JDK 17+ Setup on Windows (If Not Already Installed)
You can quickly install JDK 17 on Windows using Windows Package Manager (`winget`) or Chocolatey:

```cmd
:: Install Eclipse Temurin JDK 17 via winget (Windows 10 / 11)
winget install EclipseAdoptium.Temurin.17.JDK

:: Or install via Chocolatey
choco install openjdk17
```
Or manually download the installer from [Eclipse Temurin](https://adoptium.net/) or [Oracle Java](https://www.oracle.com/java/technologies/downloads/).

Verify that Java 17+ is accessible in your Command Prompt (`cmd.exe`) or PowerShell:
```cmd
java -version
javac -version
```

> [!TIP]
> If Windows reports `'java' is not recognized as an internal or external command`, make sure your JDK `bin` directory (e.g. `C:\Program Files\Eclipse Adoptium\jdk-17.x.x\bin`) is added to your Windows `PATH` environment variable, and that `JAVA_HOME` points to your JDK installation directory.

#### B. Install Dependencies on Windows
Run the automated batch script (double-click in File Explorer or run in CMD/PowerShell):
```cmd
:: Using the dependency installer script
install-dependencies.bat
```
*Alternatively, you can install dependencies directly using the Maven wrapper:*
```cmd
mvnw.cmd dependency:go-offline clean compile
```

#### C. Compile the Code on Windows
Compile the production domain core, systems, and test classes:
```cmd
:: In Command Prompt (CMD)
mvnw.cmd clean compile test-compile

:: In PowerShell
.\mvnw.cmd clean compile test-compile
```
*(If Maven is installed globally on your machine, you can also run `mvn clean compile test-compile`)*

#### D. Run Automated Tests on Windows
Execute the headless JUnit 5 test suite (24 unit and integration tests):
```cmd
:: In Command Prompt
mvnw.cmd test

:: In PowerShell
.\mvnw.cmd test
```

#### E. Package the Standalone Application on Windows
Create the distributable JAR package:
```cmd
:: In Command Prompt
mvnw.cmd package -DskipTests

:: In PowerShell
.\mvnw.cmd package -DskipTests
```
The compiled JAR file will be generated in the `target/` directory.

---

### Linux & macOS Setup, Compilation & Build

#### A. Make Scripts Executable
```bash
chmod +x mvnw scripts/*.sh
```

#### B. Compile the Code
```bash
./mvnw clean compile test-compile
```

#### C. Run Automated Tests
```bash
./mvnw test
```

#### D. Package the Standalone Application
```bash
./mvnw package -DskipTests
```

---

## 4. Quick Start & Running

Both Windows batch scripts (`.bat`) and Linux/macOS shell scripts (`.sh`) are provided in the [`scripts/`](file:///home/boom/projects/Lostfatility/scripts) directory for seamless execution.

### Launch Option A: JavaFX Graphical Mode (Cyberpunk 2D GUI)

#### On Windows:
**Method 1 — Launcher Script (Recommended):**
- Double-click [`scripts\run-gui.bat`](file:///home/boom/projects/Lostfatility/scripts/run-gui.bat) in Windows File Explorer, or run in terminal:
```cmd
:: Command Prompt
scripts\run-gui.bat

:: PowerShell
.\scripts\run-gui.bat
```

**Method 2 — Using Maven Wrapper:**
```cmd
:: Command Prompt
mvnw.cmd javafx:run

:: PowerShell
.\mvnw.cmd javafx:run
```

#### On Linux / macOS:
```bash
# Using launcher script
./scripts/run-gui.sh

# Or using Maven wrapper directly
./mvnw javafx:run
```

---

### Launch Option B: Terminal CLI Mode (Interactive Console)

#### On Windows:
**Method 1 — Launcher Script (Recommended):**
- Double-click [`scripts\run-cli.bat`](file:///home/boom/projects/Lostfatility/scripts/run-cli.bat) in Windows File Explorer, or run in terminal:
```cmd
:: Command Prompt
scripts\run-cli.bat

:: PowerShell
.\scripts\run-cli.bat
```

**Method 2 — Using Maven Wrapper:**
```cmd
:: Command Prompt
mvnw.cmd exec:java -Dexec.mainClass="lostfacility.cli.CliApp"

:: PowerShell
.\mvnw.cmd exec:java -Dexec.mainClass="lostfacility.cli.CliApp"
```

> [!TIP]
> **Windows Terminal & UTF-8 Encoding:** For the best visual experience in CLI mode (ANSI colors and crisp ASCII character maps), we recommend using **Windows Terminal** (standard on Windows 11). If using legacy `cmd.exe`, run `chcp 65001` before launching to ensure UTF-8 console output.

#### On Linux / macOS:
```bash
# Using launcher script
./scripts/run-cli.sh

# Or using Maven wrapper directly
./mvnw exec:java -Dexec.mainClass="lostfacility.cli.CliApp"
```

---

### Windows Troubleshooting FAQ

| Problem | Cause | Solution |
|---|---|---|
| `'java' is not recognized as an internal or external command` | JDK 17+ is not installed or not in the Windows `PATH`. | Install JDK 17 using `winget install EclipseAdoptium.Temurin.17.JDK` or add your JDK `bin` folder to the system `PATH` variable. |
| `'mvnw.cmd' is not recognized` | Running in PowerShell without relative path notation. | In PowerShell, prefix commands with `.\` (e.g. `.\mvnw.cmd compile` or `.\scripts\run-gui.bat`). |
| Strange characters or question marks in Terminal CLI | Legacy Windows `cmd.exe` code page is not set to UTF-8. | Run `chcp 65001` in the prompt, or use Windows Terminal / PowerShell. |
| JavaFX graphics window does not display | Missing graphical display or outdated graphics drivers. | Ensure your GPU display drivers are up to date, or run in Terminal CLI mode (`scripts\run-cli.bat`). |

---

## 5. Controls & Gameplay Guide

### Graphical User Interface (GUI)
| Control | Key / Action | Description |
|---|---|---|
| **Movement** | `W`, `A`, `S`, `D` or `Arrow Keys` | Move operative North, West, South, or East |
| **Attack** | `Space` or `F` / `[⚔ Attack]` | Strike adjacent or targeted enemy |
| **Interact / Take** | `G` / `[🖐 Take]` | Pick up items on current tile |
| **Talk** | `T` / `[💬 Talk]` | Initiate conversation with adjacent NPC |
| **Inspect** | `[🔍 Look]` | Examine room, entities, and environment |
| **Dialogue Choices** | `1` – `9` or Mouse Click | Select dialogue branch option |
| **Console Command** | `/` (Slash) or click prompt | Focus terminal command bar (`SYS://`) |
| **Quick Save / Load** | `[💾 Save]` / `[📂 Load]` | Save or restore checkpoint |
| **Sound Toggle** | `[🔊 Sound: ON]` button | Toggle audio mute on/off |

### Terminal CLI Commands
The terminal runner accepts natural English commands as well as rapid shorthand keys:
```text
  w, a, s, d       - Step North, West, South, East
  move <dir>       - Move in specified direction (e.g., 'move north')
  attack [target]  - Strike enemy (e.g., 'attack robot')
  take <item>      - Pick up ground item (e.g., 'take iron sword')
  use <item>       - Consume health item (e.g., 'use health potion')
  equip <item>     - Equip weapon or armor (e.g., 'equip iron sword')
  talk             - Talk to adjacent scientist or NPC
  1, 2, 3...       - Choose numbered dialogue response
  look             - Inspect current room and surroundings
  inv              - Display inventory items and equipment stats
  save <slot>      - Save game state to slot (e.g., 'save 1')
  load <slot>      - Restore game state from slot (e.g., 'load 1')
  help             - Display available commands
  quit / exit      - Exit terminal session
```

---

## 6. Campaign Authoring Guide

Custom adventure campaigns can be created without writing any Java code. Campaigns are placed in `src/main/resources/games/<campaign_name>/` containing the following JSON schemas:

### 1. `world.json` — Rooms & ASCII Layouts
Define rooms with human-readable ASCII layout strings:
```json
{
  "id": "lost_facility_world",
  "name": "The Lost Facility",
  "startingRoomId": "maintenance_room",
  "rooms": [
    {
      "id": "maintenance_room",
      "name": "Maintenance Room",
      "description": "A cold room filled with machinery and flickering emergency lights.",
      "layout": [
        "#######",
        "#..P..#",
        "#.....#",
        "#...D.#",
        "#######"
      ],
      "exits": { "south": "corridor" },
      "doors": [
        { "x": 4, "y": 3, "locked": false, "targetRoomId": "corridor" }
      ],
      "items": [
        { "itemId": "iron_sword", "x": 5, "y": 2 }
      ]
    }
  ]
}
```
**Tile Glyphs:**
- `#` = Impassable Wall
- `.` = Walkable Metallic Floor
- `D` = Door / Air Blast Hatch
- `P` = Player Spawn Position
- `X` = Exit / Evacuation Pad

### 2. `items.json` — Weapons, Armor & Consumables
```json
[
  {
    "id": "iron_sword",
    "name": "Plasma Blade",
    "description": "High-frequency energized blade cutting through armor.",
    "type": "WEAPON",
    "bonusAttack": 10,
    "bonusDefense": 0,
    "healAmount": 0
  }
]
```

### 3. `enemies.json` — Hostile Entities & AI
```json
[
  {
    "id": "robot_corridor",
    "name": "Security Robot Unit-A",
    "roomId": "corridor",
    "x": 6,
    "y": 2,
    "hp": 40,
    "maxHp": 40,
    "attack": 12,
    "defense": 3,
    "detectionRange": 4,
    "expReward": 45,
    "enemyType": "robot"
  }
]
```

### 4. `dialogue.json` — Branching Conversation Trees
```json
{
  "dr_aris_tree": [
    {
      "id": "start",
      "speaker": "Dr. Aris",
      "text": "Thank goodness! The facility's security grid has turned hostile.",
      "choices": [
        { "text": "Can you help me get through the blast door?", "nextNodeId": "give_card" }
      ]
    },
    {
      "id": "give_card",
      "speaker": "Dr. Aris",
      "text": "Take this Level 3 Security Access Card.",
      "choices": [
        { "text": "Thank you, Doctor.", "nextNodeId": "exit", "giveItemId": "access_card" }
      ]
    }
  ]
}
```

### 5. `quests.json` — Missions & Event-Driven Objectives
```json
[
  {
    "id": "escape_facility",
    "title": "Escape the Facility",
    "description": "Find an exit card, unlock the surface airway, and escape.",
    "objectives": [
      { "id": "obj_collect_card", "description": "Obtain Security Access Card", "type": "COLLECT_ITEM", "targetId": "access_card" },
      { "id": "obj_reach_exit", "description": "Reach the Facility Surface Airway", "type": "ENTER_ROOM", "targetId": "main_exit" }
    ]
  }
]
```

---

## 7. Project Structure

```text
Lostfatility/
├── pom.xml                                  # Maven dependencies & plugins
├── install-dependencies.bat                 # Windows automated dependency installer
├── mvnw / mvnw.cmd                          # Cross-platform Maven wrappers (Linux/macOS & Windows)
├── scripts/
│   ├── run-gui.bat                          # Windows JavaFX GUI launcher
│   ├── run-cli.bat                          # Windows Terminal CLI launcher
│   ├── run-gui.sh                           # Linux/macOS JavaFX GUI launcher
│   └── run-cli.sh                           # Linux/macOS Terminal CLI launcher
├── saves/                                   # Multi-slot JSON save files
├── src/
│   ├── main/
│   │   ├── java/lostfacility/
│   │   │   ├── model/                       # Domain models (World, Room, Tile, Player, Enemy, Item)
│   │   │   ├── engine/                      # GameEngine, GameState, CommandParser
│   │   │   ├── action/                      # Command Pattern actions (Move, Attack, Take, Use, Talk...)
│   │   │   ├── event/                       # Strongly typed event bus (MoveEvent, CombatEvent...)
│   │   │   ├── system/                      # CombatSystem, DialogueManager, QuestManager
│   │   │   ├── persistence/                 # JsonLoader, SaveManager (multi-slot JSON persistence)
│   │   │   ├── cli/                         # AsciiMapRenderer, CliOutputFormatter, CliApp
│   │   │   └── gui/                         # GameCanvas, AnimationController, DialogueOverlay, GameView, MainApp
│   │   └── resources/
│   │       ├── games/lost_facility/         # Demonstration campaign JSON data files
│   │       ├── sprites/                     # 32x32 pixel-art PNG textures
│   │       └── styles/theme.css             # Cyberpunk dark retro styling
│   └── test/java/lostfacility/
│       ├── engine/                          # Movement, collision, inventory, combat math, full campaign tests
│       ├── persistence/                     # JsonLoader and SaveManager unit tests
│       ├── cli/                             # CLI interactive playthrough tests
│       ├── gui/                             # AnimationController and AudioService headless tests
│       └── system/                          # Quest and Dialogue system unit tests
```

---

## 8. Automated Test Suite

The engine includes 24 automated unit and integration tests executed with JUnit 5:
- [`MovementAndCollisionTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/engine/MovementAndCollisionTest.java): Verifies boundary constraints, walkable tiles, and locked doors.
- [`InventoryAndItemTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/engine/InventoryAndItemTest.java): Verifies item pickups, equipment attack/defense calculation, and consumable healing.
- [`CombatMathTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/engine/CombatMathTest.java): Verifies damage formula `max(1, ATK - DEF)`, retaliations, and enemy defeats.
- [`CommandParserTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/engine/CommandParserTest.java): Verifies natural English parsing and directional shorthand.
- [`JsonLoaderTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/persistence/JsonLoaderTest.java) & [`SaveManagerTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/persistence/SaveManagerTest.java): Verifies multi-slot JSON persistence and state roundtrips.
- [`QuestAndDialogueTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/system/QuestAndDialogueTest.java): Verifies branching choice selection, consequence flags, and item rewards.
- [`AnimationAndAudioTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/gui/AnimationAndAudioTest.java): Verifies movement lerp calculations, screen shake, floating combat text, and headless audio synthesis.
- [`CliCampaignPlaythroughTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/cli/CliCampaignPlaythroughTest.java): Simulates full walkthrough via CLI commands from start to victory.
- [`FullCampaignIntegrationTest`](file:///home/boom/projects/Lostfatility/src/test/java/lostfacility/engine/FullCampaignIntegrationTest.java): Simulates complete canonical campaign walkthrough, mid-game checkpoint save/load, and quest victory.

Run the entire test suite anytime:
```bash
mvn test
```
Result: **24 tests run, 0 failures, 0 errors, 0 skipped.**
