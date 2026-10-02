package lostfacility.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A discrete grid cell in a room.
 */
public class Tile implements Serializable {

    private final Position position;
    private TileType type;
    private final List<Item> itemsOnGround;

    // Door specific properties
    private boolean isDoor;
    private boolean isLocked;
    private boolean isOpen;
    private String requiredKeyId;
    private String targetRoomId;
    private Position targetPlayerPos;

    public Tile(Position position, TileType type) {
        this.position = position;
        this.type = type;
        this.itemsOnGround = new ArrayList<>();
        this.isDoor = (type == TileType.DOOR);
        this.isLocked = false;
        this.isOpen = false;
    }

    public Position getPosition() {
        return position;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
        this.isDoor = (type == TileType.DOOR);
    }

    public boolean isWalkable() {
        if (type == TileType.WALL || type == TileType.VOID) {
            return false;
        }
        if (isDoor) {
            return isOpen;
        }
        return type.isInherentlyWalkable();
    }

    public List<Item> getItemsOnGround() {
        return Collections.unmodifiableList(itemsOnGround);
    }

    public void addItem(Item item) {
        if (item != null) {
            itemsOnGround.add(item);
        }
    }

    public boolean removeItem(Item item) {
        return itemsOnGround.remove(item);
    }

    public Item removeItemByName(String name) {
        if (name == null) return null;
        for (int i = 0; i < itemsOnGround.size(); i++) {
            Item item = itemsOnGround.get(i);
            if (item.getName().equalsIgnoreCase(name) || item.getId().equalsIgnoreCase(name)) {
                return itemsOnGround.remove(i);
            }
        }
        return null;
    }

    public boolean hasItems() {
        return !itemsOnGround.isEmpty();
    }

    public boolean isDoor() {
        return isDoor;
    }

    public void setDoor(boolean isDoor) {
        this.isDoor = isDoor;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }

    public void setRequiredKeyId(String requiredKeyId) {
        this.requiredKeyId = requiredKeyId;
        this.isLocked = (requiredKeyId != null && !requiredKeyId.isBlank());
    }

    public String getTargetRoomId() {
        return targetRoomId;
    }

    public void setTargetRoomId(String targetRoomId) {
        this.targetRoomId = targetRoomId;
    }

    public Position getTargetPlayerPos() {
        return targetPlayerPos;
    }

    public void setTargetPlayerPos(Position targetPlayerPos) {
        this.targetPlayerPos = targetPlayerPos;
    }

    public boolean unlock(String keyId) {
        if (!isLocked) {
            isOpen = true;
            return true;
        }
        if (requiredKeyId == null || requiredKeyId.equalsIgnoreCase(keyId)) {
            isLocked = false;
            isOpen = true;
            return true;
        }
        return false;
    }

    public char getDisplayGlyph() {
        if (isDoor) {
            return isOpen ? '/' : '+';
        }
        if (!itemsOnGround.isEmpty()) {
            return '*';
        }
        return type.getGlyph();
    }
}
