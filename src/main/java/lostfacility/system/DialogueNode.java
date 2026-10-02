package lostfacility.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A discrete dialogue node spoken by an NPC with branching choices.
 */
public class DialogueNode implements Serializable {

    private final String id;
    private final String speaker;
    private final String text;
    private final List<DialogueChoice> choices;

    public DialogueNode(String id, String speaker, String text, List<DialogueChoice> choices) {
        this.id = id;
        this.speaker = speaker;
        this.text = text;
        this.choices = (choices != null) ? new ArrayList<>(choices) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getText() {
        return text;
    }

    public List<DialogueChoice> getChoices() {
        return Collections.unmodifiableList(choices);
    }
}
