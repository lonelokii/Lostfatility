package lostfacility.persistence;

import lostfacility.engine.GameState;
import lostfacility.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SaveManagerTest {

    @Test
    @DisplayName("SaveManager serializes and restores game state with multi-slot metadata")
    void testSaveAndLoadCycle(@TempDir Path tempDir) {
        SaveManager saveManager = new SaveManager(tempDir);

        World world = new World("w", "Test World");
        Room room = Room.fromAscii("r1", "Sector 1", "Room desc", List.of(
                "#####",
                "#.P.#",
                "#####"
        ));
        world.addRoom(room);
        world.setStartingRoomId("r1");

        Player player = new Player("Survivor", new Position(2, 1));
        player.setHp(77);
        player.getInventory().addItem(Item.createWeapon("laser_blade", "Laser Blade", "Energy sword", 15));
        player.getInventory().addItem(Item.createKey("card_alpha", "Alpha Card", "Key"));
        player.getInventory().equip(player.getInventory().findByNameOrId("laser_blade").get());

        GameState originalState = new GameState(world, player);
        originalState.setFlag("core_unlocked", true);

        // Save to slot "slot_test"
        boolean saveSuccess = saveManager.save("slot_test", originalState);
        assertTrue(saveSuccess, "Saving game state should succeed");

        // Verify metadata listing
        List<SaveMetadata> saves = saveManager.listSaves();
        assertEquals(1, saves.size());
        assertEquals("slot_test", saves.get(0).slotName());
        assertEquals(77, saves.get(0).playerHp());

        // Alter memory state
        player.setHp(10);
        originalState.setFlag("core_unlocked", false);

        // Reload state
        Optional<GameState> loadedStateOpt = saveManager.load("slot_test", world);
        assertTrue(loadedStateOpt.isPresent(), "Loaded state should be present");

        GameState loadedState = loadedStateOpt.get();
        assertEquals(77, loadedState.getPlayer().getHp(), "Restored HP should match saved value");
        assertEquals(new Position(2, 1), loadedState.getPlayer().getPosition(), "Restored position should match");
        assertEquals(2, loadedState.getPlayer().getInventory().size(), "Inventory size should match");
        assertTrue(loadedState.getPlayer().getInventory().hasItem("laser_blade"));
        assertEquals("laser_blade", loadedState.getPlayer().getInventory().getEquippedWeapon().getId(), "Equipped weapon should match");
        assertTrue(loadedState.isFlag("core_unlocked"), "Flag core_unlocked should be restored");
    }
}
