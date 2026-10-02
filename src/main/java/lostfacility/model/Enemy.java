package lostfacility.model;

/**
 * Hostile actor controlled by state-based AI.
 */
public class Enemy extends Entity {

    private EnemyState state;
    private int detectionRange;
    private int expReward;
    private final String enemyType;

    public Enemy(String id, String name, Position position, int maxHp, int attack, int defense, int detectionRange, int expReward, String enemyType) {
        super(id, name, position, maxHp, attack, defense);
        this.state = EnemyState.IDLE;
        this.detectionRange = Math.max(1, detectionRange);
        this.expReward = Math.max(0, expReward);
        this.enemyType = enemyType != null ? enemyType : "enemy";
    }

    public static Enemy createSecurityRobot(String id, Position position) {
        // Base attributes from game.md §13: HP: 50, Attack: 10, Defense: 3
        return new Enemy(id, "Security Robot", position, 50, 10, 3, 4, 35, "robot");
    }

    public EnemyState getState() {
        return state;
    }

    public void setState(EnemyState state) {
        this.state = state;
    }

    public int getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(int detectionRange) {
        this.detectionRange = detectionRange;
    }

    public int getExpReward() {
        return expReward;
    }

    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }

    public String getEnemyType() {
        return enemyType;
    }

    @Override
    public int takeDamage(int amount) {
        int damage = super.takeDamage(amount);
        if (!isAlive()) {
            this.state = EnemyState.DEFEATED;
        }
        return damage;
    }
}
