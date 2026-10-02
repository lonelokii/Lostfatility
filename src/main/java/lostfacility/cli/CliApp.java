package lostfacility.cli;

import lostfacility.action.*;
import lostfacility.engine.GameEngine;
import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.persistence.JsonLoader;
import lostfacility.persistence.SaveManager;
import lostfacility.system.DialogueManager;
import lostfacility.system.Quest;
import lostfacility.system.QuestManager;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Interactive terminal command runner for The Lost Facility adventure game.
 */
public class CliApp {

    private final GameEngine engine;
    private final GameState state;
    private final EventManager eventManager;
    private final DialogueManager dialogueManager;
    private final QuestManager questManager;
    private final SaveManager saveManager;
    private final CliOutputFormatter formatter;

    private boolean running;

    public CliApp(JsonLoader.GameBundle bundle) {
        this.state = bundle.gameState();
        this.eventManager = new EventManager();
        this.dialogueManager = bundle.dialogueManager();
        this.questManager = bundle.questManager();
        this.saveManager = new SaveManager();
        this.formatter = new CliOutputFormatter();

        // Wire EventManager
        this.eventManager.subscribe(event -> {
            if (event instanceof MessageEvent me) {
                formatter.addLogMessage(me.text());
            } else {
                formatter.addLogMessage(event.getDescription());
            }
        });
        this.questManager.attachToEventManager(this.eventManager);

        this.engine = new GameEngine(state, eventManager, new lostfacility.engine.CommandParser());
        this.running = true;
    }

    public static CliApp createDefault() {
        try {
            JsonLoader loader = new JsonLoader();
            JsonLoader.GameBundle bundle = loader.loadCampaign("games/lost_facility");
            return new CliApp(bundle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Lost Facility game campaign: " + e.getMessage(), e);
        }
    }

    public void run(InputStream in, PrintStream out) {
        Scanner scanner = new Scanner(in);

        out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        out.println("║                       THE LOST FACILITY — TERMINAL                           ║");
        out.println("║            2D Text-Driven Reusable Game Engine Demonstration                 ║");
        out.println("╚══════════════════════════════════════════════════════════════════════════════╝\n");

        eventManager.publish(new MessageEvent("You awaken in the maintenance bay. Facility security is malfunctioning.", MessageEvent.Channel.NARRATIVE));

        while (running && !state.isGameOver() && !state.isGameWon()) {
            // Check victory condition: reached main_exit
            if ("main_exit".equals(state.getCurrentRoom().getId())) {
                state.setGameWon(true);
                break;
            }

            Quest activeQuest = questManager.getActiveQuest();
            out.print(formatter.renderDashboard(state, dialogueManager, activeQuest));

            if (dialogueManager.isInDialogue()) {
                out.print(" Choose option (1-" + dialogueManager.getAvailableChoices(state).size() + ") or 'end': > ");
            } else {
                out.print(" Command (WASD, look, attack, take, use, equip, talk, save, load, quit): > ");
            }

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine();
            executeCommand(line);
        }

        if (state.isGameWon()) {
            out.println("\n" + renderVictoryScreen());
        } else if (state.isGameOver()) {
            out.println("\n" + renderGameOverScreen());
        }
    }

    public ActionResult executeCommand(String line) {
        if (line == null) return ActionResult.failure("No input");
        String trimmed = line.trim();

        if (trimmed.equalsIgnoreCase("quit") || trimmed.equalsIgnoreCase("exit")) {
            this.running = false;
            return ActionResult.success("Exiting game.");
        }

        // 1. Dialogue mode input
        if (dialogueManager.isInDialogue()) {
            if (trimmed.equalsIgnoreCase("end") || trimmed.equalsIgnoreCase("leave")) {
                dialogueManager.endDialogue();
                return ActionResult.success("Left conversation.");
            }
            try {
                int choiceIndex = Integer.parseInt(trimmed) - 1;
                boolean handled = dialogueManager.chooseOption(choiceIndex, state, eventManager);
                if (handled) {
                    return ActionResult.success("Selected option " + trimmed);
                } else {
                    formatter.addLogMessage("Invalid dialogue choice: " + trimmed);
                    return ActionResult.failure("Invalid choice");
                }
            } catch (NumberFormatException e) {
                formatter.addLogMessage("Please enter a choice number.");
                return ActionResult.failure("Not a number");
            }
        }

        // 2. Custom action overrides
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("talk")) {
            String npc = lower.length() > 4 ? lower.substring(4).trim() : null;
            return engine.execute(new TalkAction(npc, dialogueManager));
        }

        if (lower.startsWith("save")) {
            String slot = lower.length() > 4 ? lower.substring(4).trim() : "quicksave";
            return engine.execute(new SaveAction(slot, saveManager));
        }

        if (lower.startsWith("load")) {
            String slot = lower.length() > 4 ? lower.substring(4).trim() : "quicksave";
            return engine.execute(new LoadAction(slot, saveManager, state.getWorld()));
        }

        // 3. Delegate to engine CommandParser
        return engine.handleInput(trimmed);
    }

    private String renderVictoryScreen() {
        return """
        ================================================================================
          ★★★ YOU ESCAPED THE FACILITY! ★★★
          The surface airway blast doors depressurize.
          Cold morning air and natural sunlight greet you as you step outside.
          The facility lockdown is behind you. You have survived The Lost Facility!
        ================================================================================
        """;
    }

    private String renderGameOverScreen() {
        return """
        ================================================================================
          ☠☠☠ YOU DIED ☠☠☠
          Your vital signs have ceased.
          The automated defense grid has sanitized the sector.
        ================================================================================
        """;
    }

    public static void main(String[] args) {
        CliApp app = createDefault();
        app.run(System.in, System.out);
    }

    public GameState getState() {
        return state;
    }

    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public String renderCurrentView() {
        return formatter.renderDashboard(state, dialogueManager, questManager.getActiveQuest());
    }
}
