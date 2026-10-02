package lostfacility.cli;

import lostfacility.engine.GameState;
import lostfacility.model.*;
import lostfacility.system.DialogueChoice;
import lostfacility.system.DialogueManager;
import lostfacility.system.Quest;
import lostfacility.system.QuestObjective;

import java.util.ArrayList;
import java.util.List;

/**
 * Formats full console UI screens with statistics HUD, room maps, quest trackers, and message logs.
 */
public class CliOutputFormatter {

    private final AsciiMapRenderer mapRenderer;
    private final List<String> messageLogBuffer = new ArrayList<>();
    private static final int MAX_LOG_LINES = 6;

    public CliOutputFormatter() {
        this(new AsciiMapRenderer(true));
    }

    public CliOutputFormatter(AsciiMapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer != null ? mapRenderer : new AsciiMapRenderer(false);
    }

    public void addLogMessage(String message) {
        if (message == null || message.isBlank()) return;
        messageLogBuffer.add(message);
        while (messageLogBuffer.size() > MAX_LOG_LINES) {
            messageLogBuffer.remove(0);
        }
    }

    public String renderDashboard(GameState state, DialogueManager dialogueManager, Quest activeQuest) {
        StringBuilder sb = new StringBuilder();
        Player p = state.getPlayer();
        Room r = state.getCurrentRoom();

        // 1. Top HUD
        sb.append("================================================================================\n");
        sb.append(String.format(" %-16s | HP: %s %3d/%-3d | ATK: %2d (+%2d) | DEF: %2d (+%2d) | LVL: %d\n",
                p.getName(),
                renderHpBar(p.getHp(), p.getMaxHp(), 10),
                p.getHp(),
                p.getMaxHp(),
                p.getAttack(),
                p.getInventory().getTotalWeaponAttackBonus(),
                p.getDefense(),
                p.getInventory().getTotalArmorDefenseBonus(),
                p.getLevel()
        ));

        String weapon = (p.getInventory().getEquippedWeapon() != null) ? p.getInventory().getEquippedWeapon().getName() : "None";
        String armor = (p.getInventory().getEquippedArmor() != null) ? p.getInventory().getEquippedArmor().getName() : "None";
        sb.append(String.format(" Weapon: %-18s | Armor: %-18s | Items: %2d/%2d\n", weapon, armor, p.getInventory().size(), p.getInventory().getCapacity()));
        sb.append("--------------------------------------------------------------------------------\n");

        // 2. Room Header
        if (r != null) {
            sb.append(" LOCATION: ").append(r.getName()).append("\n");
            sb.append(" ").append(r.getDescription()).append("\n\n");

            // 3. Map Viewport
            String mapAscii = mapRenderer.render(r, p, state.getEnemiesInCurrentRoom(), state.getNpcsInCurrentRoom());
            sb.append(mapAscii).append("\n\n");
        }

        // 4. Dialogue Box (if in conversation)
        if (dialogueManager != null && dialogueManager.isInDialogue()) {
            sb.append("--------------------------------[ DIALOGUE ]------------------------------------\n");
            var node = dialogueManager.getActiveNode();
            sb.append(" [").append(node.getSpeaker()).append("]: \"").append(node.getText()).append("\"\n\n");
            List<DialogueChoice> choices = dialogueManager.getAvailableChoices(state);
            for (int i = 0; i < choices.size(); i++) {
                sb.append("   (").append(i + 1).append(") ").append(choices.get(i).text()).append("\n");
            }
            sb.append("--------------------------------------------------------------------------------\n");
        } else {
            // 5. Active Quest
            if (activeQuest != null) {
                sb.append(" QUEST: ").append(activeQuest.getTitle()).append("\n");
                for (QuestObjective obj : activeQuest.getObjectives()) {
                    sb.append("   [").append(obj.isCompleted() ? "X" : " ").append("] ").append(obj.getDescription()).append("\n");
                }
                sb.append("\n");
            }

            // 6. Recent Logs
            sb.append(" LOGS:\n");
            if (messageLogBuffer.isEmpty()) {
                sb.append("   (Facility emergency protocols active. Awaiting orders...)\n");
            } else {
                for (String msg : messageLogBuffer) {
                    sb.append("   > ").append(msg).append("\n");
                }
            }
        }

        sb.append("================================================================================\n");
        return sb.toString();
    }

    private String renderHpBar(int current, int max, int width) {
        if (max <= 0) return "[]";
        float ratio = Math.max(0f, Math.min(1f, (float) current / max));
        int filled = Math.round(ratio * width);
        int empty = width - filled;
        return "[" + "█".repeat(filled) + "░".repeat(empty) + "]";
    }
}
