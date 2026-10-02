package lostfacility.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for all living, interactive actors in the game world.
 */
public abstract class Entity implements Serializable {

    private final String id;
    private String name;
    private Position position;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private boolean alive;

    public Entity(String id, String name, Position position, int maxHp, int attack, int defense) {
        this.id = Objects.requireNonNull(id, "Entity id must not be null");
        this.name = name != null ? name : id;
        this.position = position != null ? position : new Position(0, 0);
        this.maxHp = Math.max(1, maxHp);
        this.hp = this.maxHp;
        this.attack = Math.max(0, attack);
        this.defense = Math.max(0, defense);
        this.alive = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position must not be null");
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(hp, maxHp));
        this.alive = (this.hp > 0);
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = Math.max(1, maxHp);
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = Math.max(0, attack);
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = Math.max(0, defense);
    }

    public boolean isAlive() {
        return alive && hp > 0;
    }

    public int takeDamage(int amount) {
        if (!alive || amount <= 0) return 0;
        int damageDealt = Math.min(amount, hp);
        setHp(hp - damageDealt);
        return damageDealt;
    }

    public int heal(int amount) {
        if (!alive || amount <= 0) return 0;
        int prevHp = hp;
        setHp(hp + amount);
        return hp - prevHp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity entity)) return false;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
