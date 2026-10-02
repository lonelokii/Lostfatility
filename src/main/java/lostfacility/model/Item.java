package lostfacility.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Concrete item that can be picked up, stored in inventory, used, or equipped.
 */
public class Item implements Serializable {

    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int bonusAttack;
    private final int bonusDefense;
    private final int healAmount;

    public Item(String id, String name, String description, ItemType type, int bonusAttack, int bonusDefense, int healAmount) {
        this.id = Objects.requireNonNull(id, "Item id must not be null");
        this.name = Objects.requireNonNull(name, "Item name must not be null");
        this.description = description != null ? description : "";
        this.type = type != null ? type : ItemType.MISC;
        this.bonusAttack = bonusAttack;
        this.bonusDefense = bonusDefense;
        this.healAmount = healAmount;
    }

    // Factory methods for convenient creation
    public static Item createWeapon(String id, String name, String description, int bonusAttack) {
        return new Item(id, name, description, ItemType.WEAPON, bonusAttack, 0, 0);
    }

    public static Item createArmor(String id, String name, String description, int bonusDefense) {
        return new Item(id, name, description, ItemType.ARMOR, 0, bonusDefense, 0);
    }

    public static Item createConsumable(String id, String name, String description, int healAmount) {
        return new Item(id, name, description, ItemType.CONSUMABLE, 0, 0, healAmount);
    }

    public static Item createKey(String id, String name, String description) {
        return new Item(id, name, description, ItemType.KEY, 0, 0, 0);
    }

    public static Item createQuestItem(String id, String name, String description) {
        return new Item(id, name, description, ItemType.QUEST, 0, 0, 0);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    public int getBonusAttack() {
        return bonusAttack;
    }

    public int getBonusDefense() {
        return bonusDefense;
    }

    public int getHealAmount() {
        return healAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + type.getLabel() + ")";
    }
}
