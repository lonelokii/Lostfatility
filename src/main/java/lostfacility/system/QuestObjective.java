package lostfacility.system;

import java.io.Serializable;

/**
 * An individual milestone condition within a quest.
 */
public class QuestObjective implements Serializable {

    public enum Type {
        COLLECT_ITEM,
        ENTER_ROOM,
        DEFEAT_ENEMY,
        TALK_NPC,
        ESCAPE
    }

    private final String id;
    private final String description;
    private final Type type;
    private final String targetId;
    private boolean completed;

    public QuestObjective(String id, String description, Type type, String targetId) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.targetId = targetId;
        this.completed = false;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public String getTargetId() {
        return targetId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
