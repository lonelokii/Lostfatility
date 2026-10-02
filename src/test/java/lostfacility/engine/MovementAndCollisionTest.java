package lostfacility.engine;

import lostfacility.action.ActionResult;
import lostfacility.action.MoveAction;
import lostfacility.event.EventManager;
import lostfacility.event.MoveEvent;
import lostfacility.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MovementAndCollisionTest {

    private World world;
    private Room testRoom;
    private Player player;
    private GameState gameState;
    private EventManager eventManager;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        world = new World("test_world", "Test World");

        // 5x4 test room:
        // #####
        // #.P.#  (1,1 is walkable, 2,1 is player spawn, 3,1 is walkable)
        // #.#D#  (1,2 is walkable, 2,2 is wall, 3,2 is door)
        // #####
        List<String> layout = List.of(
                "#####",
                "#.P.#",
                "#.#D#",
                "#####"
        );
        testRoom = Room.fromAscii("room1", "Test Chamber", "A sterile room.", layout);
        Tile doorTile = testRoom.getTile(3, 2);
        doorTile.setLocked(true);
        doorTile.setRequiredKeyId("rusty_key");

        world.addRoom(testRoom);
        world.setStartingRoomId("room1");

        player = new Player("Hero", new Position(2, 1));
        gameState = new GameState(world, player);
        eventManager = new EventManager();
        engine = new GameEngine(gameState, eventManager, new CommandParser());
    }

    @Test
    @DisplayName("Player moves successfully to an adjacent walkable floor tile")
    void testWalkableMovement() {
        List<MoveEvent> moveEvents = new ArrayList<>();
        eventManager.subscribe(e -> {
            if (e instanceof MoveEvent me) {
                moveEvents.add(me);
            }
        });

        ActionResult result = engine.execute(new MoveAction(Direction.WEST));
        assertTrue(result.isSuccess(), "Movement west should succeed");
        assertEquals(new Position(1, 1), player.getPosition(), "Player should be at (1,1)");
        assertEquals(Direction.WEST, player.getFacingDirection(), "Player facing should be WEST");
        assertEquals(1, moveEvents.size(), "Exactly one MoveEvent should be emitted");
    }

    @Test
    @DisplayName("Player collision blocks walking into walls")
    void testWallCollision() {
        // Player is at (2, 1). Moving NORTH hits wall at (2, 0).
        ActionResult result = engine.execute(new MoveAction(Direction.NORTH));
        assertFalse(result.isSuccess(), "Movement north into wall should fail");
        assertEquals(new Position(2, 1), player.getPosition(), "Player position should not change");

        // Moving SOUTH hits wall at (2, 2).
        ActionResult southResult = engine.execute(new MoveAction(Direction.SOUTH));
        assertFalse(southResult.isSuccess(), "Movement south into interior wall should fail");
        assertEquals(new Position(2, 1), player.getPosition(), "Player position should not change");
    }

    @Test
    @DisplayName("Player is blocked by a locked door when lacking the required key")
    void testLockedDoorBlocksMovement() {
        // Move player to (3, 1) first
        ActionResult moveEast = engine.execute(new MoveAction(Direction.EAST));
        assertTrue(moveEast.isSuccess());
        assertEquals(new Position(3, 1), player.getPosition());

        // Attempt to move SOUTH into locked door at (3, 2)
        ActionResult moveDoor = engine.execute(new MoveAction(Direction.SOUTH));
        assertFalse(moveDoor.isSuccess(), "Movement into locked door should fail");
        assertTrue(moveDoor.message().contains("locked"), "Failure message should mention door is locked");
        assertEquals(new Position(3, 1), player.getPosition());
    }

    @Test
    @DisplayName("Player unlocks and passes through a locked door when holding the key")
    void testDoorUnlocksWithKey() {
        // Give player the required key
        Item key = Item.createKey("rusty_key", "Rusty Key", "An old iron key.");
        player.getInventory().addItem(key);

        // Move to (3, 1)
        engine.execute(new MoveAction(Direction.EAST));

        // Move SOUTH into door at (3, 2)
        ActionResult unlockResult = engine.execute(new MoveAction(Direction.SOUTH));
        assertTrue(unlockResult.isSuccess(), "Door should be unlocked and entered");
        assertTrue(testRoom.getTile(3, 2).isOpen(), "Door tile should now be open");
        assertEquals(new Position(3, 2), player.getPosition(), "Player should now be on the door tile");
    }
}
