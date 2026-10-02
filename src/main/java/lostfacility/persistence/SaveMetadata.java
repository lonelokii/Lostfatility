package lostfacility.persistence;

import java.io.Serializable;
import java.time.Instant;

/**
 * Metadata preview associated with a save game slot.
 */
public record SaveMetadata(
        String slotName,
        String timestampIso,
        String playerName,
        int playerHp,
        int playerMaxHp,
        String roomName,
        int level
) implements Serializable {

    public SaveMetadata(String slotName, Instant timestamp, String playerName, int playerHp, int playerMaxHp, String roomName, int level) {
        this(slotName, timestamp != null ? timestamp.toString() : Instant.now().toString(), playerName, playerHp, playerMaxHp, roomName, level);
    }
}
