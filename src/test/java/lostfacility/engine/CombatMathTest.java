package lostfacility.engine;

import lostfacility.action.ActionResult;
import lostfacility.action.AttackAction;
import lostfacility.event.CombatEvent;
import lostfacility.event.EventManager;
import lostfacility.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CombatMathTest {

    private GameState gameState;
    private Player player;
    private Enemy robot;
    private EventManager eventManager;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        World world = new World("w", "World");
        Room room = Room.fromAscii("r", "Room", "Desc", List.of(
                "#####",
                "#.P.#",
                "#.E.#",
                "#####"
        ));
        world.addRoom(room);
        world.setStartingRoomId("r");

        player = new Player("Player", new Position(2, 1));
        // Facing South towards Enemy at (2, 2)
        player.setFacingDirection(Direction.SOUTH);

        robot = Enemy.createSecurityRobot("robot_1", new Position(2, 2));

        gameState = new GameState(world, player);
        gameState.addEnemyToRoom("r", robot);

        eventManager = new EventManager();
        engine = new GameEngine(gameState, eventManager, new CommandParser());
    }

    @Test
    @DisplayName("Damage calculation complies with formula max(1, atk - def) + rand(-3, 3)")
    void testDamageCalculationRange() {
        // Player ATK=15, Robot DEF=3. Base diff = 12. Range = [12 - 3, 12 + 3] = [9, 15].
        List<CombatEvent> combatEvents = new ArrayList<>();
        eventManager.subscribe(e -> {
            if (e instanceof CombatEvent ce) {
                combatEvents.add(ce);
            }
        });

        int robotInitialHp = robot.getHp();
        ActionResult result = engine.execute(new AttackAction());
        assertTrue(result.isSuccess(), "Attack should succeed against adjacent facing enemy");

        assertFalse(combatEvents.isEmpty(), "CombatEvents should be published");
        CombatEvent playerAttack = combatEvents.get(0);
        assertEquals("player", playerAttack.attackerId());
        assertEquals("robot_1", playerAttack.targetId());

        int damageDealt = playerAttack.damage();
        assertTrue(damageDealt >= 9 && damageDealt <= 15, "Damage " + damageDealt + " must be between 9 and 15");
        assertEquals(robotInitialHp - damageDealt, robot.getHp(), "Robot HP should reflect damage");
    }

    @Test
    @DisplayName("Damage is at least 1 when defender defense exceeds attacker attack")
    void testMinimumDamageConstraint() {
        // Boost robot defense way above player attack
        robot.setDefense(100);

        ActionResult result = engine.execute(new AttackAction());
        assertTrue(result.isSuccess());

        // Even with 100 DEF, damage dealt should be at least 1
        assertTrue(robot.getHp() < 50, "Robot must take at least 1 damage");
    }

    @Test
    @DisplayName("Enemy retaliates when surviving player attack, and does not retaliate upon defeat")
    void testEnemyRetaliationAndDefeat() {
        // Lower robot HP so player kill it in 1 hit
        robot.setHp(5);

        List<CombatEvent> combatEvents = new ArrayList<>();
        eventManager.subscribe(e -> {
            if (e instanceof CombatEvent ce) {
                combatEvents.add(ce);
            }
        });

        int playerInitialHp = player.getHp();
        ActionResult result = engine.execute(new AttackAction());
        assertTrue(result.isSuccess());

        assertFalse(robot.isAlive(), "Robot should be defeated");
        assertEquals(EnemyState.DEFEATED, robot.getState());

        // Exactly 1 combat event should be present (player's killing blow), no retaliation
        assertEquals(1, combatEvents.size(), "Defeated enemy should not retaliate");
        assertEquals(playerInitialHp, player.getHp(), "Player should take zero damage from defeated enemy");
        assertTrue(player.getExperience() > 0, "Player should gain experience from defeated enemy");
    }
}
