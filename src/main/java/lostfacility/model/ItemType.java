package lostfacility.model;

/**
 * Item classifications determining usage and equipment slot.
 */
public enum ItemType {
    WEAPON("Weapon", true, false),
    ARMOR("Armor", true, false),
    CONSUMABLE("Consumable", false, true),
    KEY("Key", false, false),
    QUEST("Quest Item", false, false),
    MISC("Misc", false, false);

    private final String label;
    private final boolean equippable;
    private final boolean consumable;

    ItemType(String label, boolean equippable, boolean consumable) {
        this.label = label;
        this.equippable = equippable;
        this.consumable = consumable;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEquippable() {
        return equippable;
    }

    public boolean isConsumable() {
        return consumable;
    }
}
