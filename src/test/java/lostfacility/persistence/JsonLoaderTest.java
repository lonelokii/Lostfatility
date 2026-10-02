package lostfacility.persistence;

import lostfacility.model.Item;
import lostfacility.model.Room;
import lostfacility.model.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JsonLoaderTest {

    @Test
    @DisplayName("JsonLoader loads full Lost Facility campaign from JSON resources")
    void testLoadLostFacilityCampaign() throws IOException {
        JsonLoader loader = new JsonLoader();
        JsonLoader.GameBundle bundle = loader.loadCampaign("games/lost_facility");

        assertNotNull(bundle, "GameBundle must not be null");
        World world = bundle.world();
        assertNotNull(world, "World must not be null");
        assertEquals("lost_facility_world", world.getId());

        // Verify rooms
        assertEquals(5, world.getRooms().size(), "Campaign should have 5 rooms");
        assertTrue(world.hasRoom("maintenance_room"));
        assertTrue(world.hasRoom("corridor"));
        assertTrue(world.hasRoom("storage_room"));
        assertTrue(world.hasRoom("security_room"));
        assertTrue(world.hasRoom("main_exit"));

        // Verify starting room
        Room startRoom = world.getStartingRoom();
        assertNotNull(startRoom);
        assertEquals("maintenance_room", startRoom.getId());

        // Verify items placed in maintenance room
        boolean hasRustyKey = false;
        boolean hasSword = false;
        for (int y = 0; y < startRoom.getHeight(); y++) {
            for (int x = 0; x < startRoom.getWidth(); x++) {
                for (Item item : startRoom.getTile(x, y).getItemsOnGround()) {
                    if ("rusty_key".equals(item.getId())) hasRustyKey = true;
                    if ("iron_sword".equals(item.getId())) hasSword = true;
                }
            }
        }
        assertTrue(hasRustyKey, "Maintenance room should contain rusty_key on floor");
        assertTrue(hasSword, "Maintenance room should contain iron_sword on floor");

        // Verify enemies loaded into GameState
        assertEquals(0, bundle.gameState().getEnemiesInCurrentRoom().size(), "Start room should have 0 enemies");
        bundle.gameState().setCurrentRoom(world.getRoom("corridor"));
        assertEquals(1, bundle.gameState().getEnemiesInCurrentRoom().size(), "Corridor should have 1 enemy");
        assertTrue(bundle.gameState().findEnemyByNameOrId("robot_corridor").isPresent(), "Corridor enemy should be present");

        // Check door in security room
        var secRoom = world.getRoom("security_room");
        assertNotNull(secRoom.getTile(3, 3));
        assertTrue(secRoom.getTile(3, 3).isLocked(), "Blast door in security room must be locked");
        assertEquals("access_card", secRoom.getTile(3, 3).getRequiredKeyId());

        // Verify dialogue & quest managers
        assertNotNull(bundle.dialogueManager());
        assertNotNull(bundle.questManager());
        assertNotNull(bundle.questManager().getActiveQuest(), "Active quest should be registered");
        assertEquals("escape_facility", bundle.questManager().getActiveQuest().getId());
    }
}
