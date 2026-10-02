package lostfacility.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lostfacility.engine.GameState;
import lostfacility.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * Multi-slot save game persistence manager serializing and deserializing game states to JSON.
 */
public class SaveManager {

    private final Path savesDirectory;
    private final ObjectMapper mapper;

    public SaveManager() {
        this(Path.of("saves"));
    }

    public SaveManager(Path savesDirectory) {
        this.savesDirectory = savesDirectory;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ensureSavesDirectory();
    }

    private void ensureSavesDirectory() {
        try {
            if (!Files.exists(savesDirectory)) {
                Files.createDirectories(savesDirectory);
            }
        } catch (IOException e) {
            System.err.println("Failed to create saves directory: " + e.getMessage());
        }
    }

    public boolean save(String slotName, GameState state) {
        if (slotName == null || slotName.isBlank() || state == null) {
            return false;
        }
        ensureSavesDirectory();

        String filename = "save_" + sanitizeSlotName(slotName) + ".json";
        Path savePath = savesDirectory.resolve(filename);

        SaveData data = new SaveData();
        Player p = state.getPlayer();

        data.metadata = new SaveMetadata(
                slotName,
                Instant.now(),
                p.getName(),
                p.getHp(),
                p.getMaxHp(),
                state.getCurrentRoom() != null ? state.getCurrentRoom().getName() : "Unknown",
                p.getLevel()
        );

        data.currentRoomId = state.getCurrentRoom() != null ? state.getCurrentRoom().getId() : "";
        data.playerHp = p.getHp();
        data.playerMaxHp = p.getMaxHp();
        data.playerLevel = p.getLevel();
        data.playerExp = p.getExperience();
        data.playerX = p.getPosition().x();
        data.playerY = p.getPosition().y();
        data.playerFacing = p.getFacingDirection().name();

        // Inventory
        for (Item item : p.getInventory().getItems()) {
            data.inventory.add(new ItemData(item.getId(), item.getName(), item.getDescription(), item.getType().name(), item.getBonusAttack(), item.getBonusDefense(), item.getHealAmount()));
        }
        if (p.getInventory().getEquippedWeapon() != null) {
            data.equippedWeaponId = p.getInventory().getEquippedWeapon().getId();
        }
        if (p.getInventory().getEquippedArmor() != null) {
            data.equippedArmorId = p.getInventory().getEquippedArmor().getId();
        }

        // World flags
        data.flags.putAll(state.getFlags());

        try {
            mapper.writeValue(savePath.toFile(), data);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write save file: " + e.getMessage());
            return false;
        }
    }

    public Optional<GameState> load(String slotName, World worldTemplate) {
        if (slotName == null || worldTemplate == null) return Optional.empty();

        String filename = "save_" + sanitizeSlotName(slotName) + ".json";
        Path savePath = savesDirectory.resolve(filename);
        if (!Files.exists(savePath)) {
            return Optional.empty();
        }

        try {
            SaveData data = mapper.readValue(savePath.toFile(), SaveData.class);

            Player player = new Player(data.metadata.playerName(), new Position(data.playerX, data.playerY));
            player.setMaxHp(data.playerMaxHp);
            player.setHp(data.playerHp);
            player.setFacingDirection(Direction.fromString(data.playerFacing));
            if (data.playerExp > 0) {
                player.addExperience(data.playerExp);
            }

            // Restore items
            for (ItemData id : data.inventory) {
                Item item = new Item(id.id, id.name, id.description, ItemType.valueOf(id.type), id.bonusAttack, id.bonusDefense, id.healAmount);
                player.getInventory().addItem(item);
                if (id.id.equals(data.equippedWeaponId)) {
                    player.getInventory().equip(item);
                } else if (id.id.equals(data.equippedArmorId)) {
                    player.getInventory().equip(item);
                }
            }

            GameState state = new GameState(worldTemplate, player);
            if (worldTemplate.hasRoom(data.currentRoomId)) {
                state.setCurrentRoom(worldTemplate.getRoom(data.currentRoomId));
            }

            for (Map.Entry<String, Boolean> entry : data.flags.entrySet()) {
                state.setFlag(entry.getKey(), entry.getValue());
            }

            return Optional.of(state);
        } catch (IOException e) {
            System.err.println("Failed to read save file: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<SaveMetadata> listSaves() {
        ensureSavesDirectory();
        List<SaveMetadata> result = new ArrayList<>();
        File[] files = savesDirectory.toFile().listFiles((dir, name) -> name.startsWith("save_") && name.endsWith(".json"));
        if (files == null) return result;

        for (File f : files) {
            try {
                SaveData data = mapper.readValue(f, SaveData.class);
                if (data.metadata != null) {
                    result.add(data.metadata);
                }
            } catch (Exception ignored) {}
        }
        result.sort((a, b) -> b.timestampIso().compareTo(a.timestampIso()));
        return result;
    }

    private String sanitizeSlotName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public static class SaveData {
        public SaveMetadata metadata;
        public String currentRoomId;
        public int playerHp;
        public int playerMaxHp;
        public int playerLevel;
        public int playerExp;
        public int playerX;
        public int playerY;
        public String playerFacing;
        public String equippedWeaponId;
        public String equippedArmorId;
        public List<ItemData> inventory = new ArrayList<>();
        public Map<String, Boolean> flags = new HashMap<>();
    }

    public static class ItemData {
        public String id;
        public String name;
        public String description;
        public String type;
        public int bonusAttack;
        public int bonusDefense;
        public int healAmount;

        public ItemData() {}

        public ItemData(String id, String name, String description, String type, int bonusAttack, int bonusDefense, int healAmount) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.bonusAttack = bonusAttack;
            this.bonusDefense = bonusDefense;
            this.healAmount = healAmount;
        }
    }
}
