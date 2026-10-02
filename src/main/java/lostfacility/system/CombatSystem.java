package lostfacility.system;

import lostfacility.event.CombatEvent;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.model.Enemy;
import lostfacility.model.EnemyState;
import lostfacility.model.Player;

import java.util.Random;

/**
 * Dedicated combat engine subsystem managing damage calculations, hit resolution, and retaliation.
 */
public class CombatSystem {

    private final Random random;

    public CombatSystem() {
        this(new Random());
    }

    public CombatSystem(Random random) {
        this.random = random != null ? random : new Random();
    }

    /**
     * Calculates damage following PRD formula: max(1, attacker - defender) + variance.
     */
    public int calculateDamage(int attack, int defense, int minVariance, int maxVariance) {
        int diff = attack - defense;
        int spread = maxVariance - minVariance + 1;
        int variance = (spread > 0) ? random.nextInt(spread) + minVariance : 0;
        return Math.max(1, diff + variance);
    }

    /**
     * Executes player striking an enemy target, handling retaliation if target survives.
     */
    public CombatResult resolveCombatRound(Player player, Enemy target, EventManager events) {
        if (player == null || target == null || !player.isAlive()) {
            return new CombatResult(0, 0, false, false);
        }

        // 1. Player attacks enemy
        int playerDamage = calculateDamage(player.getEffectiveAttack(), target.getDefense(), -3, 3);
        int actualDamage = target.takeDamage(playerDamage);
        boolean targetDefeated = !target.isAlive();

        CombatEvent playerAttackEvent = new CombatEvent(
                player.getId(),
                player.getName(),
                target.getId(),
                target.getName(),
                actualDamage,
                targetDefeated,
                target.getHp()
        );
        events.publish(playerAttackEvent);

        if (targetDefeated) {
            target.setState(EnemyState.DEFEATED);
            player.addExperience(target.getExpReward());
            events.publish(new MessageEvent("You defeated " + target.getName() + " and gained " + target.getExpReward() + " XP!", MessageEvent.Channel.COMBAT));
            return new CombatResult(actualDamage, 0, true, false);
        }

        // 2. Enemy retaliates if still alive and adjacent
        int enemyDamage = 0;
        boolean playerDefeated = false;

        if (target.isAlive() && target.getPosition().isAdjacentTo(player.getPosition())) {
            enemyDamage = calculateDamage(target.getAttack(), player.getEffectiveDefense(), -2, 2);
            int actualEnemyDamage = player.takeDamage(enemyDamage);
            playerDefeated = !player.isAlive();

            CombatEvent enemyRetaliationEvent = new CombatEvent(
                    target.getId(),
                    target.getName(),
                    player.getId(),
                    player.getName(),
                    actualEnemyDamage,
                    playerDefeated,
                    player.getHp()
                    );
            events.publish(enemyRetaliationEvent);

            if (playerDefeated) {
                events.publish(new MessageEvent("You were defeated by " + target.getName() + "!", MessageEvent.Channel.SYSTEM));
            }
        }

        return new CombatResult(actualDamage, enemyDamage, targetDefeated, playerDefeated);
    }

    public record CombatResult(
            int damageDealt,
            int damageTaken,
            boolean isEnemyDefeated,
            boolean isPlayerDefeated
    ) {}
}
