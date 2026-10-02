package lostfacility.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Container holding all rooms in the game world.
 */
public class World implements Serializable {

    private final String id;
    private final String name;
    private String startingRoomId;
    private final Map<String, Room> rooms;

    public World(String id, String name) {
        this.id = Objects.requireNonNull(id, "World id must not be null");
        this.name = name != null ? name : id;
        this.rooms = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStartingRoomId() {
        return startingRoomId;
    }

    public void setStartingRoomId(String startingRoomId) {
        this.startingRoomId = startingRoomId;
    }

    public void addRoom(Room room) {
        if (room != null) {
            rooms.put(room.getId(), room);
            if (startingRoomId == null) {
                startingRoomId = room.getId();
            }
        }
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room getStartingRoom() {
        return startingRoomId != null ? rooms.get(startingRoomId) : null;
    }

    public Map<String, Room> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    public boolean hasRoom(String roomId) {
        return rooms.containsKey(roomId);
    }
}
