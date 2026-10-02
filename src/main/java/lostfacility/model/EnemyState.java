package lostfacility.model;

/**
 * State machine states for hostile enemy entities.
 */
public enum EnemyState {
    IDLE,
    DETECT,
    CHASE,
    ATTACK,
    DEFEATED
}
