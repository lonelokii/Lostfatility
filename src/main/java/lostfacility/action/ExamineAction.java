package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;
import lostfacility.event.MessageEvent;
import lostfacility.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Inspects the current room, specific items, or visible entities.
 */
public record ExamineAction(String targetQuery) implements GameAction {

    public ExamineAction() {
        this(null);
    }

    @Override
    public ActionResult execute(GameState state, EventManager events) {
        Room room = state.getCurrentRoom();
        Player player = state.getPlayer();

        if (room == null || player == null) {
            return ActionResult.failure("Cannot examine: invalid state.");
        }

        // If no target or general look
        if (targetQuery == null || targetQuery.isBlank() || targetQuery.equalsIgnoreCase("room") || targetQuery.equalsIgnoreCase("around")) {
            StringBuilder sb = new StringBuilder();
            sb.append(room.getName()).append("\n");
            sb.append(room.getDescription()).append("\n");

            // Exits
            if (!room.getExits().isEmpty()) {
                sb.append("Exits: ");
                for (Direction d : room.getExits().keySet()) {
                    sb.append(d.getDisplayName()).append(" ");
                }
                sb.append("\n");
            }

            // Enemies
            List<Enemy> enemies = state.getEnemiesInCurrentRoom();
            for (Enemy enemy : enemies) {
                if (enemy.isAlive()) {
                    sb.append("A hostile ").append(enemy.getName()).append(" is here! (").append(enemy.getHp()).append("/").append(enemy.getMaxHp()).append(" HP)\n");
                }
            }

            // Ground items
            Tile playerTile = room.getTile(player.getPosition());
            if (playerTile != null && playerTile.hasItems()) {
                sb.append("Items at your feet: ");
                for (Item item : playerTile.getItemsOnGround()) {
                    sb.append(item.getName()).append(" ");
                }
                sb.append("\n");
            }

            MessageEvent msg = new MessageEvent(sb.toString().trim(), MessageEvent.Channel.NARRATIVE);
            events.publish(msg);
            return ActionResult.success(msg.text(), msg);
        }

        // Check inventory for item
        Optional<Item> invItem = player.getInventory().findByNameOrId(targetQuery);
        if (invItem.isPresent()) {
            Item item = invItem.get();
            String desc = item.getName() + " [" + item.getType().getLabel() + "]: " + item.getDescription();
            if (item.getBonusAttack() > 0) desc += " (+ " + item.getBonusAttack() + " ATK)";
            if (item.getBonusDefense() > 0) desc += " (+ " + item.getBonusDefense() + " DEF)";
            if (item.getHealAmount() > 0) desc += " (Heals " + item.getHealAmount() + " HP)";

            MessageEvent msg = new MessageEvent(desc, MessageEvent.Channel.NARRATIVE);
            events.publish(msg);
            return ActionResult.success(desc, msg);
        }

        // Check enemy
        Optional<Enemy> enemyOpt = state.findEnemyByNameOrId(targetQuery);
        if (enemyOpt.isPresent()) {
            Enemy enemy = enemyOpt.get();
            String desc = enemy.getName() + ": " + enemy.getHp() + "/" + enemy.getMaxHp() + " HP, ATK " + enemy.getAttack() + ", DEF " + enemy.getDefense();
            MessageEvent msg = new MessageEvent(desc, MessageEvent.Channel.NARRATIVE);
            events.publish(msg);
            return ActionResult.success(desc, msg);
        }

        return ActionResult.failure("You don't see any '" + targetQuery + "' here.");
    }
}
