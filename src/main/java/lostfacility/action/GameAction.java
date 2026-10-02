package lostfacility.action;

import lostfacility.engine.GameState;
import lostfacility.event.EventManager;

/**
 * Command pattern interface encapsulating an atomic player or game intent.
 */
@FunctionalInterface
public interface GameAction {

    ActionResult execute(GameState state, EventManager events);
}
