package lostfacility.system;

import java.io.Serializable;

/**
 * A selectable player response option in a dialogue node.
 */
public record DialogueChoice(
        String text,
        String nextNodeId,
        String requiredFlag,
        String grantFlag,
        String giveItemId
) implements Serializable {

    public DialogueChoice(String text, String nextNodeId) {
        this(text, nextNodeId, null, null, null);
    }
}
