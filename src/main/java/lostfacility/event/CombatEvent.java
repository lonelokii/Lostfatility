package lostfacility.event;

import java.time.Instant;

/**
 * Emitted when combat occurs between an attacker and a target.
 */
public record CombatEvent(
        String attackerId,
        String attackerName,
        String targetId,
        String targetName,
        int damage,
        boolean isDefeated,
        int targetRemainingHp,
        Instant timestamp
) implements GameEvent {

    public CombatEvent(String attackerId, String attackerName, String targetId, String targetName, int damage, boolean isDefeated, int targetRemainingHp) {
        this(attackerId, attackerName, targetId, targetName, damage, isDefeated, targetRemainingHp, Instant.now());
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getDescription() {
        if (isDefeated) {
            return attackerName + " struck " + targetName + " for " + damage + " damage. " + targetName + " was defeated!";
        }
        return attackerName + " struck " + targetName + " for " + damage + " damage (" + targetRemainingHp + " HP remaining).";
    }
}
