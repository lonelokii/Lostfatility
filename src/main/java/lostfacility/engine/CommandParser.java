package lostfacility.engine;

import lostfacility.action.*;
import lostfacility.model.Direction;
import lostfacility.model.Player;

import java.util.Optional;

/**
 * Translates natural text inputs and shorthand keys into strongly typed GameAction commands.
 */
public class CommandParser {

    public Optional<GameAction> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String raw = input.trim();
        String lower = raw.toLowerCase();
        String[] tokens = lower.split("\\s+");
        String verb = tokens[0];

        // 1. Shorthand direction movements
        Direction dir = Direction.fromString(verb);
        if (dir != null && tokens.length == 1) {
            return Optional.of(new MoveAction(dir));
        }

        // 2. Verb-based commands
        return switch (verb) {
            case "move", "go", "walk" -> {
                if (tokens.length > 1) {
                    Direction targetDir = Direction.fromString(tokens[1]);
                    if (targetDir != null) {
                        yield Optional.of(new MoveAction(targetDir));
                    }
                }
                yield Optional.empty();
            }

            case "attack", "hit", "strike", "fight" -> {
                String target = (tokens.length > 1) ? raw.substring(raw.indexOf(' ')).trim() : null;
                yield Optional.of(new AttackAction(target));
            }

            case "take", "pickup", "grab", "get" -> {
                String item = (tokens.length > 1) ? raw.substring(raw.indexOf(' ')).trim() : null;
                yield Optional.of(new TakeAction(item));
            }

            case "use", "drink", "consume" -> {
                if (tokens.length > 1) {
                    String item = raw.substring(raw.indexOf(' ')).trim();
                    yield Optional.of(new UseAction(item));
                }
                yield Optional.empty();
            }

            case "equip", "wield", "wear" -> {
                if (tokens.length > 1) {
                    String item = raw.substring(raw.indexOf(' ')).trim();
                    yield Optional.of(new EquipAction(item));
                }
                yield Optional.empty();
            }

            case "look", "l", "examine", "inspect", "check" -> {
                String target = (tokens.length > 1) ? raw.substring(raw.indexOf(' ')).trim() : null;
                yield Optional.of(new ExamineAction(target));
            }

            case "inv", "inventory", "i" -> Optional.of((state, events) -> {
                Player p = state.getPlayer();
                StringBuilder sb = new StringBuilder("INVENTORY:\n");
                if (p.getInventory().getItems().isEmpty()) {
                    sb.append("  (empty)\n");
                } else {
                    for (var item : p.getInventory().getItems()) {
                        sb.append("  - ").append(item.getName());
                        if (item.equals(p.getInventory().getEquippedWeapon())) {
                            sb.append(" [EQUIPPED WEAPON]");
                        } else if (item.equals(p.getInventory().getEquippedArmor())) {
                            sb.append(" [EQUIPPED ARMOR]");
                        }
                        sb.append("\n");
                    }
                }
                return ActionResult.success(sb.toString().trim());
            });

            default -> Optional.empty();
        };
    }
}
