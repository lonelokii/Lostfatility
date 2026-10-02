package lostfacility.engine;

import lostfacility.action.ActionResult;
import lostfacility.action.GameAction;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.model.Direction;
import lostfacility.model.Enemy;
import lostfacility.model.EnemyState;
import lostfacility.model.Player;
import lostfacility.model.Position;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Central orchestrator connecting GameState, Action execution, EventManager, and CommandParser.
 */
public class GameEngine {

    private final GameState state;
    private final EventManager eventManager;
    private final CommandParser commandParser;

    public GameEngine(GameState state) {
        this(state, new EventManager(), new CommandParser());
    }

    public GameEngine(GameState state, EventManager eventManager, CommandParser commandParser) {
        this.state = Objects.requireNonNull(state, "GameState must not be null");
        this.eventManager = Objects.requireNonNull(eventManager, "EventManager must not be null");
        this.commandParser = Objects.requireNonNull(commandParser, "CommandParser must not be null");
    }

    public GameState getState() {
        return state;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public CommandParser getCommandParser() {
        return commandParser;
    }

    /**
     * Executes a GameAction, updates simulation state, triggers enemy turns if applicable, and returns ActionResult.
     */
    public ActionResult execute(GameAction action) {
        if (action == null) {
            return ActionResult.failure("No action specified.");
        }

        if (state.isGameOver()) {
            return ActionResult.failure("Game is over. Restart or load a save.");
        }

        ActionResult result = action.execute(state, eventManager);

        // If action succeeded and game is not won or over, run enemy turns
        if (result.isSuccess() && !state.isGameOver() && !state.isGameWon()) {
            processEnemyTurns();
        }

        return result;
    }

    /**
     * Parses raw command line text and dispatches resulting action.
     */
    public ActionResult handleInput(String inputText) {
        Optional<GameAction> actionOpt = commandParser.parse(inputText);
        if (actionOpt.isEmpty()) {
            String error = "Unknown command: '" + (inputText != null ? inputText.trim() : "") + "'. Type 'look', 'move <dir>', 'attack', 'take <item>', 'use <item>', or 'inv'.";
            MessageEvent errEvent = new MessageEvent(error, MessageEvent.Channel.ERROR);
            eventManager.publish(errEvent);
            return ActionResult.failure(error, errEvent);
        }
        return execute(actionOpt.get());
    }

    /**
     * Step 8 & 14 in game.md: Simple state-based enemy AI.
     * Enemies detect player, step towards player, or attack if adjacent.
     */
    private void processEnemyTurns() {
        Player player = state.getPlayer();
        if (player == null || !player.isAlive()) return;

        List<Enemy> enemies = state.getEnemiesInCurrentRoom();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            Position enemyPos = enemy.getPosition();
            Position playerPos = player.getPosition();
            int distance = enemyPos.manhattanDistance(playerPos);

            if (distance <= enemy.getDetectionRange()) {
                enemy.setState(EnemyState.CHASE);

                if (distance == 1) {
                    // Adjacent: Enemy attacks player!
                    enemy.setState(EnemyState.ATTACK);
                    int baseDiff = enemy.getAttack() - player.getEffectiveDefense();
                    int damage = Math.max(1, baseDiff);
                    int actualDamage = player.takeDamage(damage);

                    MessageEvent attackMsg = new MessageEvent(
                            enemy.getName() + " attacked you for " + actualDamage + " damage! (" + player.getHp() + "/" + player.getMaxHp() + " HP remaining)",
                            MessageEvent.Channel.COMBAT
                    );
                    eventManager.publish(attackMsg);

                    if (!player.isAlive()) {
                        state.setGameOver(true);
                        MessageEvent deathMsg = new MessageEvent("You collapsed from your wounds. GAME OVER.", MessageEvent.Channel.SYSTEM);
                        eventManager.publish(deathMsg);
                        break;
                    }
                } else {
                    // Move 1 tile closer to player if tile is walkable
                    Position nextStep = calculateNextStepTowards(enemyPos, playerPos);
                    if (nextStep != null && state.getCurrentRoom().isWalkable(nextStep) && state.findEnemyAt(nextStep).isEmpty() && !nextStep.equals(playerPos)) {
                        enemy.setPosition(nextStep);
                    }
                }
            } else {
                enemy.setState(EnemyState.IDLE);
            }
        }
    }

    private Position calculateNextStepTowards(Position from, Position to) {
        int dx = Integer.compare(to.x(), from.x());
        int dy = Integer.compare(to.y(), from.y());

        // Prefer moving along the axis with larger distance
        if (Math.abs(to.x() - from.x()) >= Math.abs(to.y() - from.y())) {
            Position stepX = from.add(dx, 0);
            if (state.getCurrentRoom().isWalkable(stepX) && state.findEnemyAt(stepX).isEmpty()) {
                return stepX;
            }
            Position stepY = from.add(0, dy);
            if (state.getCurrentRoom().isWalkable(stepY) && state.findEnemyAt(stepY).isEmpty()) {
                return stepY;
            }
        } else {
            Position stepY = from.add(0, dy);
            if (state.getCurrentRoom().isWalkable(stepY) && state.findEnemyAt(stepY).isEmpty()) {
                return stepY;
            }
            Position stepX = from.add(dx, 0);
            if (state.getCurrentRoom().isWalkable(stepX) && state.findEnemyAt(stepX).isEmpty()) {
                return stepX;
            }
        }
        return null;
    }
}
