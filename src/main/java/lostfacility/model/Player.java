package lostfacility.model;

/**
 * Player character managed by user input.
 */
public class Player extends Entity {

    private final Inventory inventory;
    private Direction facingDirection;
    private int level;
    private int experience;

    public Player(String name, Position position) {
        // Base attributes from game.md: HP: 100, Attack: 15, Defense: 5, Level: 1
        super("player", name != null ? name : "Explorer", position, 100, 15, 5);
        this.inventory = new Inventory();
        this.facingDirection = Direction.SOUTH;
        this.level = 1;
        this.experience = 0;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        if (facingDirection != null) {
            this.facingDirection = facingDirection;
        }
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public void addExperience(int amount) {
        if (amount <= 0) return;
        this.experience += amount;
        // Simple level progression: level * 100 exp
        int expNeeded = level * 100;
        if (experience >= expNeeded) {
            level++;
            setMaxHp(getMaxHp() + 15);
            heal(15);
            setAttack(getAttack() + 3);
            setDefense(getDefense() + 1);
        }
    }

    public int getEffectiveAttack() {
        return getAttack() + inventory.getTotalWeaponAttackBonus();
    }

    public int getEffectiveDefense() {
        return getDefense() + inventory.getTotalArmorDefenseBonus();
    }
}
