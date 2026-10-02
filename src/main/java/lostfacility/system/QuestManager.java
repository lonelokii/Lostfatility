package lostfacility.system;

import lostfacility.event.*;

import java.io.Serializable;
import java.util.*;

/**
 * Manages quest lifecycles and listens to GameEvents to automatically advance quest objectives.
 */
public class QuestManager implements Serializable {

    private final Map<String, Quest> quests = new HashMap<>();
    private String activeQuestId;

    public void registerQuest(Quest quest) {
        if (quest != null) {
            quests.put(quest.getId(), quest);
            if (activeQuestId == null) {
                activeQuestId = quest.getId();
                quest.setState(QuestState.ACTIVE);
            }
        }
    }

    public Quest getActiveQuest() {
        return activeQuestId != null ? quests.get(activeQuestId) : null;
    }

    public void setActiveQuest(String questId) {
        if (quests.containsKey(questId)) {
            this.activeQuestId = questId;
            quests.get(questId).setState(QuestState.ACTIVE);
        }
    }

    public Collection<Quest> getAllQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }

    /**
     * Wires the quest manager to the event manager to automatically respond to game world actions.
     */
    public void attachToEventManager(EventManager eventManager) {
        if (eventManager == null) return;

        eventManager.subscribe(event -> {
            Quest active = getActiveQuest();
            if (active == null || active.getState() == QuestState.COMPLETED) {
                return;
            }

            boolean updated = false;

            if (event instanceof ItemEvent ie && ie.actionType() == ItemEvent.ActionType.TAKE) {
                updated = active.checkAndUpdateObjectives(QuestObjective.Type.COLLECT_ITEM, ie.itemId());
            } else if (event instanceof MoveEvent me && me.roomChanged()) {
                updated = active.checkAndUpdateObjectives(QuestObjective.Type.ENTER_ROOM, me.roomId());
            } else if (event instanceof CombatEvent ce && ce.isDefeated()) {
                updated = active.checkAndUpdateObjectives(QuestObjective.Type.DEFEAT_ENEMY, ce.targetId());
            }

            if (updated) {
                if (active.getState() == QuestState.COMPLETED) {
                    eventManager.publish(new MessageEvent("★ QUEST COMPLETE: " + active.getTitle() + "!", MessageEvent.Channel.SYSTEM));
                } else {
                    eventManager.publish(new MessageEvent("Quest objective updated for: " + active.getTitle(), MessageEvent.Channel.SYSTEM));
                }
            }
        });
    }

    public String formatActiveQuestStatus() {
        Quest active = getActiveQuest();
        if (active == null) return "No active quest.";

        StringBuilder sb = new StringBuilder();
        sb.append("QUEST: ").append(active.getTitle()).append(" [").append(active.getState()).append("]\n");
        sb.append(active.getDescription()).append("\nObjectives:\n");

        for (QuestObjective obj : active.getObjectives()) {
            sb.append("  [").append(obj.isCompleted() ? "X" : " ").append("] ").append(obj.getDescription()).append("\n");
        }
        return sb.toString().trim();
    }
}
