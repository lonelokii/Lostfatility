package lostfacility.event;

import java.time.Instant;

/**
 * General narrative and system log event displayed in the game log.
 */
public record MessageEvent(
        String text,
        Channel channel,
        Instant timestamp
) implements GameEvent {

    public enum Channel {
        NARRATIVE,
        COMBAT,
        SYSTEM,
        DIALOGUE,
        ERROR
    }

    public MessageEvent(String text, Channel channel) {
        this(text, channel, Instant.now());
    }

    public MessageEvent(String text) {
        this(text, Channel.SYSTEM, Instant.now());
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getDescription() {
        return text;
    }
}
