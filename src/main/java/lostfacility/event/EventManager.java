package lostfacility.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-safe event bus distributing GameEvents to registered listeners (views, logs, controllers).
 */
public class EventManager {

    private final List<Consumer<GameEvent>> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<GameEvent> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(Consumer<GameEvent> listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void publish(GameEvent event) {
        if (event == null) return;
        for (Consumer<GameEvent> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                System.err.println("Error dispatching event " + event.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public void publishAll(Iterable<? extends GameEvent> events) {
        if (events == null) return;
        for (GameEvent event : events) {
            publish(event);
        }
    }

    public void clear() {
        listeners.clear();
    }
}
