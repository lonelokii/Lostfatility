package lostfacility.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A structured quest containing sequential or concurrent objectives.
 */
public class Quest implements Serializable {

    private final String id;
    private final String title;
    private final String description;
    private final List<QuestObjective> objectives;
    private QuestState state;

    public Quest(String id, String title, String description, List<QuestObjective> objectives) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.objectives = (objectives != null) ? new ArrayList<>(objectives) : new ArrayList<>();
        this.state = QuestState.NOT_STARTED;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<QuestObjective> getObjectives() {
        return Collections.unmodifiableList(objectives);
    }

    public QuestState getState() {
        return state;
    }

    public void setState(QuestState state) {
        this.state = state;
    }

    public boolean isAllObjectivesCompleted() {
        for (QuestObjective obj : objectives) {
            if (!obj.isCompleted()) return false;
        }
        return true;
    }

    public boolean checkAndUpdateObjectives(QuestObjective.Type type, String targetId) {
        if (state == QuestState.COMPLETED || state == QuestState.FAILED) return false;
        boolean updated = false;

        for (QuestObjective obj : objectives) {
            if (!obj.isCompleted() && obj.getType() == type) {
                if (obj.getTargetId() == null || obj.getTargetId().equalsIgnoreCase(targetId)) {
                    obj.setCompleted(true);
                    updated = true;
                }
            }
        }

        if (updated) {
            if (isAllObjectivesCompleted()) {
                this.state = QuestState.COMPLETED;
            } else {
                this.state = QuestState.OBJECTIVE_UPDATED;
            }
        }
        return updated;
    }
}
