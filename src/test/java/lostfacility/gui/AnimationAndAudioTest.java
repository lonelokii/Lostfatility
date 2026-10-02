package lostfacility.gui;

import lostfacility.event.CombatEvent;
import lostfacility.event.EventManager;
import lostfacility.event.MoveEvent;
import lostfacility.model.Direction;
import lostfacility.model.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnimationAndAudioTest {

    @Test
    @DisplayName("AudioService operates without errors in headless mode")
    void testAudioServiceHeadless() {
        AudioService audio = new AudioService();
        assertFalse(audio.isMuted());

        audio.toggleMute();
        assertTrue(audio.isMuted());

        audio.toggleMute();
        assertFalse(audio.isMuted());

        // Should not throw exceptions in headless mode
        assertDoesNotThrow(() -> {
            audio.play(AudioService.SoundEffect.STEP);
            audio.play(AudioService.SoundEffect.ATTACK);
            audio.play(AudioService.SoundEffect.HIT);
            audio.play(AudioService.SoundEffect.DEFEAT);
            audio.play(AudioService.SoundEffect.ITEM_PICKUP);
            audio.play(AudioService.SoundEffect.DOOR_OPEN);
            audio.play(AudioService.SoundEffect.DIALOGUE);
            audio.play(AudioService.SoundEffect.VICTORY);
            audio.play(AudioService.SoundEffect.GAME_OVER);
        });

        EventManager events = new EventManager();
        assertDoesNotThrow(() -> audio.attachToEventManager(events));

        // Publish events to verify event listeners execute cleanly
        assertDoesNotThrow(() -> {
            events.publish(new MoveEvent("player", new Position(0, 0), new Position(1, 0), Direction.EAST, "room-1", false));
            events.publish(new CombatEvent("player", "Player", "robot-1", "Security Robot", 15, false, 35));
        });

        audio.shutdown();
    }

    @Test
    @DisplayName("AnimationController movement interpolation calculates smooth-step coordinates")
    void testMovementInterpolation() {
        AnimationController.MoveInterpolation interp = new AnimationController.MoveInterpolation(
                0.0, 0.0,
                10.0, 0.0,
                0.150,
                new double[]{0.0}
        );

        assertEquals(0.0, interp.getProgress(), 0.001);
        assertEquals(0.0, interp.getInterpolatedX(), 0.001);
        assertFalse(interp.isComplete());

        // Advance half-way
        interp.elapsedRef()[0] = 0.075;
        assertEquals(0.5, interp.getProgress(), 0.001);
        assertEquals(5.0, interp.getInterpolatedX(), 0.001);

        // Advance to finish
        interp.elapsedRef()[0] = 0.150;
        assertTrue(interp.isComplete());
        assertEquals(10.0, interp.getInterpolatedX(), 0.001);
    }

    @Test
    @DisplayName("FloatingText updates lifetime and expires correctly")
    void testFloatingTextLifecycle() {
        FloatingText ft = new FloatingText("-25 HP", 100, 100, null, 0.5, -20.0);
        assertFalse(ft.isExpired());

        ft.update(0.25);
        assertFalse(ft.isExpired());

        ft.update(0.30);
        assertTrue(ft.isExpired());
    }

    @Test
    @DisplayName("AnimationController adds and manages combat effects and screen shake")
    void testCombatEffectAndScreenShake() {
        AnimationController controller = new AnimationController(null);

        controller.addCombatEffect(5.0, 5.0, "slash");
        assertEquals(1, controller.getActiveEffects().size());
        assertEquals(5.0, controller.getActiveEffects().get(0).getTileX());
        assertEquals(5.0, controller.getActiveEffects().get(0).getTileY());

        controller.triggerScreenShake(4.0, 0.10);
        controller.resetAll();

        assertEquals(0, controller.getActiveEffects().size());
        assertEquals(0.0, controller.getShakeOffsetX());
        assertEquals(0.0, controller.getShakeOffsetY());
    }
}
