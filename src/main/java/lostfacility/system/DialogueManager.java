package lostfacility.system;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.ItemEvent;
import lostfacility.event.MessageEvent;
import lostfacility.model.Item;

import java.io.Serializable;
import java.util.*;

/**
 * Manages conversation flow, branching decisions, and consequence triggers.
 */
public class DialogueManager implements Serializable {

    // TreeId -> (NodeId -> DialogueNode)
    private final Map<String, Map<String, DialogueNode>> trees = new HashMap<>();

    private String activeTreeId;
    private DialogueNode activeNode;

    public void registerTree(String treeId, List<DialogueNode> nodes) {
        if (treeId == null || nodes == null) return;
        Map<String, DialogueNode> nodeMap = new HashMap<>();
        for (DialogueNode node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        trees.put(treeId, nodeMap);
    }

    public boolean startDialogue(String treeId, String startNodeId, EventManager events) {
        Map<String, DialogueNode> tree = trees.get(treeId);
        if (tree == null || !tree.containsKey(startNodeId)) {
            return false;
        }

        this.activeTreeId = treeId;
        this.activeNode = tree.get(startNodeId);

        if (events != null) {
            emitCurrentNodeMessage(events);
        }
        return true;
    }

    public boolean isInDialogue() {
        return activeNode != null;
    }

    public DialogueNode getActiveNode() {
        return activeNode;
    }

    public List<DialogueChoice> getAvailableChoices(GameState state) {
        if (activeNode == null) return Collections.emptyList();
        List<DialogueChoice> available = new ArrayList<>();
        for (DialogueChoice choice : activeNode.getChoices()) {
            if (choice.requiredFlag() == null || state.isFlag(choice.requiredFlag())) {
                available.add(choice);
            }
        }
        return available;
    }

    public boolean chooseOption(int choiceIndex, GameState state, EventManager events) {
        if (activeNode == null) return false;
        List<DialogueChoice> available = getAvailableChoices(state);

        if (choiceIndex < 0 || choiceIndex >= available.size()) {
            return false;
        }

        DialogueChoice selected = available.get(choiceIndex);

        // Apply grantFlag if present
        if (selected.grantFlag() != null && !selected.grantFlag().isBlank()) {
            state.setFlag(selected.grantFlag(), true);
        }

        // Apply giveItemId if present
        if (selected.giveItemId() != null && !selected.giveItemId().isBlank()) {
            Item item = Item.createQuestItem(selected.giveItemId(), "Access Card", "Keycard to open the main facility exit.");
            state.getPlayer().getInventory().addItem(item);
            if (events != null) {
                ItemEvent itemEvent = new ItemEvent(ItemEvent.ActionType.TAKE, item.getId(), item.getName(), state.getPlayer().getName(), "Received from NPC");
                events.publish(itemEvent);
                events.publish(new MessageEvent("Received " + item.getName() + "!", MessageEvent.Channel.SYSTEM));
            }
        }

        // Advance to next node or end dialogue
        String nextId = selected.nextNodeId();
        if (nextId == null || nextId.equalsIgnoreCase("exit") || nextId.equalsIgnoreCase("end")) {
            endDialogue();
            if (events != null) {
                events.publish(new MessageEvent("Dialogue concluded.", MessageEvent.Channel.DIALOGUE));
            }
            return true;
        }

        Map<String, DialogueNode> tree = trees.get(activeTreeId);
        if (tree != null && tree.containsKey(nextId)) {
            this.activeNode = tree.get(nextId);
            if (events != null) {
                emitCurrentNodeMessage(events);
            }
            return true;
        } else {
            endDialogue();
            return true;
        }
    }

    public void endDialogue() {
        this.activeTreeId = null;
        this.activeNode = null;
    }

    private void emitCurrentNodeMessage(EventManager events) {
        if (activeNode == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(activeNode.getSpeaker()).append("]: \"").append(activeNode.getText()).append("\"\n");
        List<DialogueChoice> choices = activeNode.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            sb.append("  ").append(i + 1).append(") ").append(choices.get(i).text()).append("\n");
        }
        events.publish(new MessageEvent(sb.toString().trim(), MessageEvent.Channel.DIALOGUE));
    }
}
