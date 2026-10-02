package lostfacility.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Player inventory maintaining carrying capacity and equipped gear slots.
 */
public class Inventory implements Serializable {

    public static final int DEFAULT_CAPACITY = 16;

    private final int capacity;
    private final List<Item> items;
    private Item equippedWeapon;
    private Item equippedArmor;

    public Inventory() {
        this(DEFAULT_CAPACITY);
    }

    public Inventory(int capacity) {
        this.capacity = capacity > 0 ? capacity : DEFAULT_CAPACITY;
        this.items = new ArrayList<>();
    }

    public int getCapacity() {
        return capacity;
    }

    public int size() {
        return items.size();
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean addItem(Item item) {
        if (item == null || isFull()) {
            return false;
        }
        return items.add(item);
    }

    public boolean removeItem(Item item) {
        if (item == null) return false;
        return items.remove(item);
    }

    public Optional<Item> findByNameOrId(String query) {
        if (query == null || query.isBlank()) return Optional.empty();
        String q = query.trim().toLowerCase();
        for (Item item : items) {
            if (item.getName().toLowerCase().contains(q) || item.getId().toLowerCase().equals(q)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public boolean hasItem(String query) {
        return findByNameOrId(query).isPresent();
    }

    public boolean equip(Item item) {
        if (item == null || !items.contains(item)) {
            return false;
        }
        if (item.getType() == ItemType.WEAPON) {
            this.equippedWeapon = item;
            return true;
        } else if (item.getType() == ItemType.ARMOR) {
            this.equippedArmor = item;
            return true;
        }
        return false;
    }

    public void unequipWeapon() {
        this.equippedWeapon = null;
    }

    public void unequipArmor() {
        this.equippedArmor = null;
    }

    public Item getEquippedWeapon() {
        return equippedWeapon;
    }

    public Item getEquippedArmor() {
        return equippedArmor;
    }

    public int getTotalWeaponAttackBonus() {
        return equippedWeapon != null ? equippedWeapon.getBonusAttack() : 0;
    }

    public int getTotalArmorDefenseBonus() {
        return equippedArmor != null ? equippedArmor.getBonusDefense() : 0;
    }
}
