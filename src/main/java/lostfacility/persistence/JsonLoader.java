package lostfacility.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lostfacility.engine.GameState;
import lostfacility.model.*;
import lostfacility.system.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads complete external game campaigns from JSON files.
 */
public class JsonLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public record GameBundle(
            World world,
            Player player,
            GameState gameState,
            DialogueManager dialogueManager,
            QuestManager questManager
    ) {}

    public GameBundle loadCampaign(String basePath) throws IOException {
        // 1. Items
        Map<String, Item> items = loadItems(resolvePath(basePath, "items.json"));

        // 2. World & Rooms
        World world = loadWorld(resolvePath(basePath, "world.json"), items);

        // 3. Player setup
        Room startRoom = world.getStartingRoom();
        Position spawnPos = (startRoom != null) ? startRoom.getDefaultSpawnPosition() : new Position(1, 1);
        Player player = new Player("Investigator", spawnPos);

        GameState gameState = new GameState(world, player);

        // 4. Enemies & NPCs
        loadEnemies(resolvePath(basePath, "enemies.json"), gameState);
        loadNpcs(resolvePath(basePath, "npcs.json"), gameState);

        // 5. Dialogue
        DialogueManager dialogueManager = new DialogueManager();
        loadDialogue(resolvePath(basePath, "dialogue.json"), dialogueManager);

        // 6. Quests
        QuestManager questManager = new QuestManager();
        loadQuests(resolvePath(basePath, "quests.json"), questManager);

        return new GameBundle(world, player, gameState, dialogueManager, questManager);
    }

    private InputStream resolvePath(String basePath, String filename) throws IOException {
        // Try file path first
        Path path = Path.of(basePath, filename);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }

        // Try classpath resources
        String resourcePath = "/" + basePath.replace('\\', '/') + "/" + filename;
        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in != null) {
            return in;
        }

        // Alternative classpath relative
        in = getClass().getClassLoader().getResourceAsStream(basePath + "/" + filename);
        if (in != null) {
            return in;
        }

        throw new IOException("Cannot locate game data file: " + filename + " at path " + basePath);
    }

    private Map<String, Item> loadItems(InputStream in) throws IOException {
        Map<String, Item> items = new HashMap<>();
        JsonNode root = mapper.readTree(in);
        for (JsonNode node : root) {
            String id = node.get("id").asText();
            String name = node.get("name").asText();
            String desc = node.get("description").asText();
            ItemType type = ItemType.valueOf(node.get("type").asText().toUpperCase());
            int atk = node.has("bonusAttack") ? node.get("bonusAttack").asInt() : 0;
            int def = node.has("bonusDefense") ? node.get("bonusDefense").asInt() : 0;
            int heal = node.has("healAmount") ? node.get("healAmount").asInt() : 0;

            items.put(id, new Item(id, name, desc, type, atk, def, heal));
        }
        return items;
    }

    private World loadWorld(InputStream in, Map<String, Item> itemRegistry) throws IOException {
        JsonNode root = mapper.readTree(in);
        String worldId = root.get("id").asText();
        String worldName = root.get("name").asText();
        String startingRoomId = root.get("startingRoomId").asText();

        World world = new World(worldId, worldName);
        world.setStartingRoomId(startingRoomId);

        for (JsonNode rNode : root.get("rooms")) {
            String id = rNode.get("id").asText();
            String name = rNode.get("name").asText();
            String desc = rNode.get("description").asText();

            List<String> layout = new ArrayList<>();
            for (JsonNode row : rNode.get("layout")) {
                layout.add(row.asText());
            }

            Room room = Room.fromAscii(id, name, desc, layout);

            // Exits
            if (rNode.has("exits")) {
                JsonNode exitsNode = rNode.get("exits");
                for (Iterator<String> it = exitsNode.fieldNames(); it.hasNext(); ) {
                    String dirStr = it.next();
                    Direction dir = Direction.fromString(dirStr);
                    if (dir != null) {
                        room.addExit(dir, exitsNode.get(dirStr).asText());
                    }
                }
            }

            // Locked doors configuration
            if (rNode.has("doors")) {
                for (JsonNode dNode : rNode.get("doors")) {
                    int dx = dNode.get("x").asInt();
                    int dy = dNode.get("y").asInt();
                    Tile tile = room.getTile(dx, dy);
                    if (tile != null) {
                        tile.setDoor(true);
                        tile.setLocked(dNode.has("locked") && dNode.get("locked").asBoolean());
                        if (dNode.has("keyRequiredId")) {
                            tile.setRequiredKeyId(dNode.get("keyRequiredId").asText());
                        }
                        if (dNode.has("targetRoomId")) {
                            tile.setTargetRoomId(dNode.get("targetRoomId").asText());
                        }
                    }
                }
            }

            // Initial ground items
            if (rNode.has("items")) {
                for (JsonNode iNode : rNode.get("items")) {
                    String itemId = iNode.get("itemId").asText();
                    int ix = iNode.get("x").asInt();
                    int iy = iNode.get("y").asInt();
                    Item item = itemRegistry.get(itemId);
                    if (item != null) {
                        room.placeItem(item, ix, iy);
                    }
                }
            }

            world.addRoom(room);
        }

        return world;
    }

    private void loadEnemies(InputStream in, GameState state) throws IOException {
        JsonNode root = mapper.readTree(in);
        for (JsonNode node : root) {
            String id = node.get("id").asText();
            String name = node.get("name").asText();
            String roomId = node.get("roomId").asText();
            int x = node.get("x").asInt();
            int y = node.get("y").asInt();
            int hp = node.get("hp").asInt();
            int atk = node.get("attack").asInt();
            int def = node.get("defense").asInt();
            int range = node.get("detectionRange").asInt();
            int exp = node.get("expReward").asInt();
            String type = node.has("enemyType") ? node.get("enemyType").asText() : "enemy";

            Enemy enemy = new Enemy(id, name, new Position(x, y), hp, atk, def, range, exp, type);
            state.addEnemyToRoom(roomId, enemy);
        }
    }

    private void loadNpcs(InputStream in, GameState state) throws IOException {
        JsonNode root = mapper.readTree(in);
        for (JsonNode node : root) {
            String id = node.get("id").asText();
            String name = node.get("name").asText();
            String roomId = node.get("roomId").asText();
            int x = node.get("x").asInt();
            int y = node.get("y").asInt();
            String dialogueTreeId = node.has("dialogueTreeId") ? node.get("dialogueTreeId").asText() : null;

            Npc npc = new Npc(id, name, new Position(x, y), dialogueTreeId);
            state.addNpcToRoom(roomId, npc);
        }
    }

    private void loadDialogue(InputStream in, DialogueManager manager) throws IOException {
        JsonNode root = mapper.readTree(in);
        for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
            String treeId = it.next();
            List<DialogueNode> nodes = new ArrayList<>();

            for (JsonNode nNode : root.get(treeId)) {
                String id = nNode.get("id").asText();
                String speaker = nNode.get("speaker").asText();
                String text = nNode.get("text").asText();

                List<DialogueChoice> choices = new ArrayList<>();
                if (nNode.has("choices")) {
                    for (JsonNode cNode : nNode.get("choices")) {
                        String cText = cNode.get("text").asText();
                        String nextId = cNode.get("nextNodeId").asText();
                        String reqFlag = cNode.has("requiredFlag") ? cNode.get("requiredFlag").asText() : null;
                        String grantFlag = cNode.has("grantFlag") ? cNode.get("grantFlag").asText() : null;
                        String giveItem = cNode.has("giveItemId") ? cNode.get("giveItemId").asText() : null;

                        choices.add(new DialogueChoice(cText, nextId, reqFlag, grantFlag, giveItem));
                    }
                }
                nodes.add(new DialogueNode(id, speaker, text, choices));
            }
            manager.registerTree(treeId, nodes);
        }
    }

    private void loadQuests(InputStream in, QuestManager manager) throws IOException {
        JsonNode root = mapper.readTree(in);
        for (JsonNode qNode : root) {
            String id = qNode.get("id").asText();
            String title = qNode.get("title").asText();
            String desc = qNode.get("description").asText();

            List<QuestObjective> objectives = new ArrayList<>();
            for (JsonNode oNode : qNode.get("objectives")) {
                String oid = oNode.get("id").asText();
                String oDesc = oNode.get("description").asText();
                QuestObjective.Type type = QuestObjective.Type.valueOf(oNode.get("type").asText().toUpperCase());
                String targetId = oNode.has("targetId") ? oNode.get("targetId").asText() : null;

                objectives.add(new QuestObjective(oid, oDesc, type, targetId));
            }

            manager.registerQuest(new Quest(id, title, desc, objectives));
        }
    }
}
