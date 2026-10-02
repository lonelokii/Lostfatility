package lostfacility.action;

import lostfacility.event.GameEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result returned by GameAction execution, encapsulating outcome status, message, and emitted events.
 */
public record ActionResult(
        boolean isSuccess,
        String message,
        List<GameEvent> events
) {

    public ActionResult {
        events = (events != null) ? Collections.unmodifiableList(new ArrayList<>(events)) : List.of();
    }

    public static ActionResult success(String message, GameEvent... events) {
        return new ActionResult(true, message, List.of(events));
    }

    public static ActionResult success(String message, List<GameEvent> events) {
        return new ActionResult(true, message, events);
    }

    public static ActionResult failure(String message, GameEvent... events) {
        return new ActionResult(false, message, List.of(events));
    }
}
