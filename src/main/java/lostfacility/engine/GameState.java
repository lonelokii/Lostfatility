package lostfacility.engine;

import lostfacility.model.*;

import java.io.Serializable;
import java.util.*;

/**
 * Root state model encapsulating current world, player, enemy instances, and world flags.
 */
public class GameState implements Serializable {

    private final World world;
    private Room currentRoom;
    private final Player player;
    private final Map<String, List<Enemy>> roomEnemies;
    private final Map<String, List<Npc>> roomNpcs;
    private final Map<String, Boolean> flags;
    private boolean gameWon;
    private boolean gameOver;

    public GameState(World world, Player player) {
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.player = Objects.requireNonNull(player, "Player must not be null");
        this.currentRoom = world.getStartingRoom();
        this.roomEnemies = new HashMap<>();
        this.roomNpcs = new HashMap<>();
        this.flags = new HashMap<>();
        this.gameWon = false;
        this.gameOver = false;

        if (this.currentRoom != null && player.getPosition() == null) {
            player.setPosition(currentRoom.getDefaultSpawnPosition());
        }
    }

    public World getWorld() {
        return world;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemiesInCurrentRoom() {
        if (currentRoom == null) return Collections.emptyList();
        return roomEnemies.computeIfAbsent(currentRoom.getId(), k -> new ArrayList<>());
    }

    public void addEnemyToRoom(String roomId, Enemy enemy) {
        if (roomId != null && enemy != null) {
            roomEnemies.computeIfAbsent(roomId, k -> new ArrayList<>()).add(enemy);
        }
    }

    public List<Npc> getNpcsInCurrentRoom() {
        if (currentRoom == null) return Collections.emptyList();
        return roomNpcs.computeIfAbsent(currentRoom.getId(), k -> new ArrayList<>());
    }

    public void addNpcToRoom(String roomId, Npc npc) {
        if (roomId != null && npc != null) {
            roomNpcs.computeIfAbsent(roomId, k -> new ArrayList<>()).add(npc);
        }
    }

    public Optional<Npc> findNpcAt(Position pos) {
        if (pos == null) return Optional.empty();
        for (Npc npc : getNpcsInCurrentRoom()) {
            if (npc.getPosition().equals(pos)) {
                return Optional.of(npc);
            }
        }
        return Optional.empty();
    }

    public Optional<Npc> findNpcByNameOrId(String query) {
        if (query == null || query.isBlank()) return Optional.empty();
        String q = query.trim().toLowerCase();
        for (Npc npc : getNpcsInCurrentRoom()) {
            if (npc.getName().toLowerCase().contains(q) || npc.getId().equalsIgnoreCase(q)) {
                return Optional.of(npc);
            }
        }
        return Optional.empty();
    }

    public Optional<Enemy> findEnemyAt(Position pos) {
        if (pos == null) return Optional.empty();
        for (Enemy enemy : getEnemiesInCurrentRoom()) {
            if (enemy.isAlive() && enemy.getPosition().equals(pos)) {
                return Optional.of(enemy);
            }
        }
        return Optional.empty();
    }

    public Optional<Enemy> findEnemyByNameOrId(String query) {
        if (query == null || query.isBlank()) return Optional.empty();
        String q = query.trim().toLowerCase();
        for (Enemy enemy : getEnemiesInCurrentRoom()) {
            if (enemy.isAlive() && (enemy.getName().toLowerCase().contains(q) || enemy.getId().equalsIgnoreCase(q))) {
                return Optional.of(enemy);
            }
        }
        return Optional.empty();
    }

    public Optional<Enemy> findAdjacentEnemy(Position pos) {
        if (pos == null) return Optional.empty();
        for (Enemy enemy : getEnemiesInCurrentRoom()) {
            if (enemy.isAlive() && enemy.getPosition().isAdjacentTo(pos)) {
                return Optional.of(enemy);
            }
        }
        return Optional.empty();
    }

    public boolean isFlag(String key) {
        return flags.getOrDefault(key, false);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    public Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public boolean isGameOver() {
        return gameOver || (player != null && !player.isAlive());
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
