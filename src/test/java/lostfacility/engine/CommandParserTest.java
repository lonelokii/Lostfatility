package lostfacility.engine;

import lostfacility.action.*;
import lostfacility.model.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    @DisplayName("Parser decodes directional shorthand and movement verbs")
    void testMovementCommands() {
        Optional<GameAction> w = parser.parse("w");
        assertTrue(w.isPresent() && w.get() instanceof MoveAction ma && ma.direction() == Direction.NORTH);

        Optional<GameAction> s = parser.parse("s");
        assertTrue(s.isPresent() && s.get() instanceof MoveAction ma && ma.direction() == Direction.SOUTH);

        Optional<GameAction> moveEast = parser.parse("move east");
        assertTrue(moveEast.isPresent() && moveEast.get() instanceof MoveAction ma && ma.direction() == Direction.EAST);

        Optional<GameAction> goWest = parser.parse("go west");
        assertTrue(goWest.isPresent() && goWest.get() instanceof MoveAction ma && ma.direction() == Direction.WEST);
    }

    @Test
    @DisplayName("Parser decodes action verbs into concrete GameActions")
    void testActionCommands() {
        Optional<GameAction> attack = parser.parse("attack");
        assertTrue(attack.isPresent() && attack.get() instanceof AttackAction);

        Optional<GameAction> attackTarget = parser.parse("attack security robot");
        assertTrue(attackTarget.isPresent() && attackTarget.get() instanceof AttackAction aa && "security robot".equals(aa.targetQuery()));

        Optional<GameAction> take = parser.parse("take rusty key");
        assertTrue(take.isPresent() && take.get() instanceof TakeAction ta && "rusty key".equals(ta.itemQuery()));

        Optional<GameAction> use = parser.parse("use health potion");
        assertTrue(use.isPresent() && use.get() instanceof UseAction ua && "health potion".equals(ua.itemQuery()));

        Optional<GameAction> equip = parser.parse("equip iron sword");
        assertTrue(equip.isPresent() && equip.get() instanceof EquipAction ea && "iron sword".equals(ea.itemQuery()));

        Optional<GameAction> look = parser.parse("look");
        assertTrue(look.isPresent() && look.get() instanceof ExamineAction);

        Optional<GameAction> inv = parser.parse("inventory");
        assertTrue(inv.isPresent());
    }

    @Test
    @DisplayName("Parser gracefully returns empty optional on invalid or empty input")
    void testInvalidCommands() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
        assertTrue(parser.parse("foobar gibberish").isEmpty());
    }
}
