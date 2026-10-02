package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.CombatEvent;
import lostfacility.event.EventManager;
import lostfacility.event.GameEvent;
import lostfacility.event.MessageEvent;
import lostfacility.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Executes a combat attack turn, including target selection, damage calculation, and enemy retaliation.
 */
public record AttackAction(String targetQuery) implements GameAction {

    private static final Random RNG = new Random();

    public AttackAction() {
        this(null);
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        Player player = state.getPlayer();
        if (player == null || !player.isAlive()) {
            return ActionResult.failure("You cannot attack.");
        }

        Enemy target = resolveTarget(state, player);
        if (target == null) {
            return ActionResult.failure("There is nothing to attack nearby.");
        }

        List<GameEvent> emittedEvents = new ArrayList<>();

        // 1. Player attacks target
        int baseDiff = player.getEffectiveAttack() - target.getDefense();
        int variance = RNG.nextInt(7) - 3; // -3 to +3
        int playerDamage = Math.max(1, baseDiff + variance);

        int actualDamage = target.takeDamage(playerDamage);
        boolean isDefeated = !target.isAlive();

        CombatEvent playerAttackEvent = new CombatEvent(
                player.getId(),
                player.getName(),
                target.getId(),
                target.getName(),
                actualDamage,
                isDefeated,
                target.getHp()
        );
        events.publish(playerAttackEvent);
        emittedEvents.add(playerAttackEvent);

        // Check if enemy defeated
        if (isDefeated) {
            player.addExperience(target.getExpReward());
            MessageEvent expMsg = new MessageEvent("You gained " + target.getExpReward() + " experience.", MessageEvent.Channel.SYSTEM);
            events.publish(expMsg);
            emittedEvents.add(expMsg);
            return ActionResult.success("You defeated the " + target.getName() + "!", emittedEvents);
        }

        // 2. Enemy retaliates if still alive and adjacent
        if (target.isAlive() && target.getPosition().isAdjacentTo(player.getPosition())) {
            int enemyBaseDiff = target.getAttack() - player.getEffectiveDefense();
            int enemyVariance = RNG.nextInt(5) - 2; // -2 to +2
            int enemyDamage = Math.max(1, enemyBaseDiff + enemyVariance);

            int actualEnemyDamage = player.takeDamage(enemyDamage);
            boolean playerDefeated = !player.isAlive();

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
            emittedEvents.add(enemyRetaliationEvent);

            if (playerDefeated) {
                state.setGameOver(true);
                MessageEvent deathMsg = new MessageEvent("YOU DIED. Game over.", MessageEvent.Channel.SYSTEM);
                events.publish(deathMsg);
                emittedEvents.add(deathMsg);
                return ActionResult.failure("You were defeated by the " + target.getName() + "!", playerAttackEvent, enemyRetaliationEvent, deathMsg);
            }
        }

        return ActionResult.success("Struck " + target.getName() + " for " + actualDamage + " damage.", emittedEvents);
    }

    private Enemy resolveTarget(GameState state, Player player) {
        Position playerPos = player.getPosition();

        // 1. If explicit query provided, look for enemy with that name/id that is adjacent
        if (targetQuery != null && !targetQuery.isBlank()) {
            Optional<Enemy> namedEnemy = state.findEnemyByNameOrId(targetQuery);
            if (namedEnemy.isPresent() && namedEnemy.get().getPosition().isAdjacentTo(playerPos)) {
                return namedEnemy.get();
            }
        }

        // 2. Check facing tile
        Direction facing = player.getFacingDirection();
        if (facing != null) {
            Position facingPos = playerPos.add(facing);
            Optional<Enemy> facingEnemy = state.findEnemyAt(facingPos);
            if (facingEnemy.isPresent() && facingEnemy.get().isAlive()) {
                return facingEnemy.get();
            }
        }

        // 3. Fallback to any adjacent living enemy
        return state.findAdjacentEnemy(playerPos).orElse(null);
    }
}
