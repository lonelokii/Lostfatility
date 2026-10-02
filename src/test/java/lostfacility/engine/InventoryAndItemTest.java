package lostfacility.engine;

import lostfacility.action.*;
import lostfacility.event.EventManager;
import lostfacility.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryAndItemTest {

    private GameEngine engine;
    private Player player;
    private Room room;

    @BeforeEach
    void setUp() {
        World world = new World("w", "Test World");
        room = Room.fromAscii("r", "Room", "Desc", List.of(
                "#####",
                "#.P.#",
                "#####"
        ));
        world.addRoom(room);
        world.setStartingRoomId("r");

        player = new Player("Player", new Position(2, 1));
        GameState state = new GameState(world, player);
        engine = new GameEngine(state, new EventManager(), new CommandParser());
    }

    @Test
    @DisplayName("TakeAction picks up item from ground into player inventory")
    void testPickUpItem() {
        Item sword = Item.createWeapon("iron_sword", "Iron Sword", "A sharp blade.", 10);
        room.placeItem(sword, 2, 1);

        assertTrue(room.getTile(2, 1).hasItems(), "Tile should have item before pickup");

        ActionResult result = engine.execute(new TakeAction("Iron Sword"));
        assertTrue(result.isSuccess(), "TakeAction should succeed");
        assertEquals(1, player.getInventory().size(), "Inventory should have 1 item");
        assertTrue(player.getInventory().hasItem("iron_sword"), "Player should have sword");
        assertFalse(room.getTile(2, 1).hasItems(), "Tile should no longer have item");
    }

    @Test
    @DisplayName("Inventory enforces capacity limit")
    void testInventoryCapacityLimit() {
        Inventory smallInv = new Inventory(2);
        Item i1 = Item.createKey("k1", "Key 1", "key");
        Item i2 = Item.createKey("k2", "Key 2", "key");
        Item i3 = Item.createKey("k3", "Key 3", "key");

        assertTrue(smallInv.addItem(i1));
        assertTrue(smallInv.addItem(i2));
        assertFalse(smallInv.addItem(i3), "Third item should exceed capacity of 2");
        assertEquals(2, smallInv.size());
    }

    @Test
    @DisplayName("UseAction consumes health potion and restores player HP")
    void testConsumePotion() {
        player.setHp(60); // Damage player
        Item potion = Item.createConsumable("health_potion", "Health Potion", "Restores 30 HP", 30);
        player.getInventory().addItem(potion);

        ActionResult result = engine.execute(new UseAction("health_potion"));
        assertTrue(result.isSuccess(), "Using potion should succeed");
        assertEquals(90, player.getHp(), "Player HP should be restored to 90");
        assertFalse(player.getInventory().hasItem("health_potion"), "Potion should be removed after use");
    }

    @Test
    @DisplayName("Equipping weapon increases player effective attack power")
    void testEquipWeaponIncreasesAttack() {
        int baseAttack = player.getEffectiveAttack();
        assertEquals(15, baseAttack);

        Item sword = Item.createWeapon("iron_sword", "Iron Sword", "ATK +10", 10);
        player.getInventory().addItem(sword);

        ActionResult equipResult = engine.execute(new EquipAction("Iron Sword"));
        assertTrue(equipResult.isSuccess(), "EquipAction should succeed");
        assertEquals(sword, player.getInventory().getEquippedWeapon(), "Sword should be equipped");
        assertEquals(25, player.getEffectiveAttack(), "Effective attack should now be 15 + 10 = 25");
    }
}
