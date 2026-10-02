package lostfacility.gui;

import lostfacility.event.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modular audio service delivering retro sci-fi sound effects.
 * Supports synthesis of 8-bit sound tones with graceful, silent fallback
 * if audio devices are unavailable or running in a headless environment.
 */
public class AudioService {

    public enum SoundEffect {
        STEP(220, 0.04, 0.15),
        ATTACK(440, 0.06, 0.25),
        HIT(140, 0.08, 0.35),
        DEFEAT(90, 0.15, 0.40),
        ITEM_PICKUP(660, 0.08, 0.25),
        DOOR_OPEN(300, 0.10, 0.25),
        DIALOGUE(520, 0.03, 0.12),
        VICTORY(880, 0.30, 0.40),
        GAME_OVER(110, 0.40, 0.40);

        final int frequency;
        final double durationSeconds;
        final double volume;

        SoundEffect(int frequency, double durationSeconds, double volume) {
            this.frequency = frequency;
            this.durationSeconds = durationSeconds;
            this.volume = volume;
        }
    }

    private boolean muted = false;
    private final ExecutorService audioExecutor;

    public AudioService() {
        this.audioExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LostFacility-Audio");
            t.setDaemon(true);
            return t;
        });
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void toggleMute() {
        this.muted = !this.muted;
    }

    public void play(SoundEffect sfx) {
        if (muted || sfx == null) return;

        audioExecutor.submit(() -> {
            try {
                generateTone(sfx.frequency, sfx.durationSeconds, sfx.volume);
            } catch (Throwable ignored) {
                // Graceful silent fallback
            }
        });
    }

    /**
     * Synthesizes a clean 8-bit retro wave tone via javax.sound.sampled PCM line.
     */
    private void generateTone(int freq, double durationSeconds, double volume) {
        try {
            float sampleRate = 22050f;
            int numSamples = (int) (durationSeconds * sampleRate);
            byte[] buffer = new byte[numSamples];

            for (int i = 0; i < numSamples; i++) {
                double time = i / sampleRate;
                // Decay envelope
                double envelope = Math.max(0.0, 1.0 - (double) i / numSamples);
                // Frequency slight slide downwards for punchy retro impact
                double currentFreq = freq * (1.0 - 0.2 * ((double) i / numSamples));
                double sin = Math.sin(2.0 * Math.PI * currentFreq * time);
                // Slight square distortion for retro 8-bit timbre
                double sample = Math.signum(sin) * 0.4 + sin * 0.6;
                buffer[i] = (byte) (sample * volume * envelope * 127);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format, buffer.length);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
            }
        } catch (Throwable ignored) {
            // Headless / no sound hardware: safely ignore
        }
    }

    /**
     * Wires the audio service to game events for automatic sound cues.
     */
    public void attachToEventManager(EventManager eventManager) {
        if (eventManager == null) return;

        eventManager.subscribe(event -> {
            if (event instanceof MoveEvent me) {
                if (me.roomChanged()) {
                    play(SoundEffect.DOOR_OPEN);
                } else {
                    play(SoundEffect.STEP);
                }
            } else if (event instanceof CombatEvent ce) {
                if (ce.isDefeated()) {
                    play(SoundEffect.DEFEAT);
                } else {
                    play(SoundEffect.HIT);
                }
            } else if (event instanceof ItemEvent ie) {
                play(SoundEffect.ITEM_PICKUP);
            } else if (event instanceof MessageEvent me) {
                if (me.channel() == MessageEvent.Channel.DIALOGUE) {
                    play(SoundEffect.DIALOGUE);
                } else if (me.text().contains("GAME OVER")) {
                    play(SoundEffect.GAME_OVER);
                } else if (me.text().contains("VICTORY") || me.text().contains("escaped")) {
                    play(SoundEffect.VICTORY);
                }
            }
        });
    }

    public void shutdown() {
        audioExecutor.shutdownNow();
    }
}
