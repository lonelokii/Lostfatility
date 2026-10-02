package lostfacility.model;

/**
 * Friendly non-player character that can engage in dialogue with the player.
 */
public class Npc extends Entity {

    private String dialogueTreeId;

    public Npc(String id, String name, Position position, String dialogueTreeId) {
        super(id, name, position, 100, 0, 10);
        this.dialogueTreeId = dialogueTreeId;
    }

    public String getDialogueTreeId() {
        return dialogueTreeId;
    }

    public void setDialogueTreeId(String dialogueTreeId) {
        this.dialogueTreeId = dialogueTreeId;
    }
}
