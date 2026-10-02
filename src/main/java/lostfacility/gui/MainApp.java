package lostfacility.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lostfacility.engine.CommandParser;
import lostfacility.engine.GameEngine;
import lostfacility.event.EventManager;
import lostfacility.persistence.JsonLoader;
import lostfacility.system.DialogueManager;
import lostfacility.system.QuestManager;

/**
 * Main JavaFX graphical application entry point for The Lost Facility Engine.
 */
public class MainApp extends Application {

    private AudioService audioService;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Load campaign data bundle
            JsonLoader loader = new JsonLoader();
            JsonLoader.GameBundle bundle = loader.loadCampaign("games/lost_facility");

            // 2. Initialize Event Bus and connect Quest Manager
            EventManager eventManager = new EventManager();
            DialogueManager dialogueManager = bundle.dialogueManager();
            QuestManager questManager = bundle.questManager();
            questManager.attachToEventManager(eventManager);

            // 3. Game Engine orchestrator
            CommandParser commandParser = new CommandParser();
            GameEngine engine = new GameEngine(bundle.gameState(), eventManager, commandParser);

            // 4. Audio Service with silent fallback
            audioService = new AudioService();
            audioService.attachToEventManager(eventManager);

            // 5. Game Canvas viewport with 60 FPS animation timer
            GameCanvas canvas = new GameCanvas(740, 540);
            canvas.setGameState(bundle.gameState());
            canvas.attachToEventManager(eventManager);

            // 6. Dialogue Choice Overlay
            DialogueOverlay dialogueOverlay = new DialogueOverlay();

            // 7. Full UI Layout
            GameView gameView = new GameView(engine, canvas, dialogueOverlay, audioService, dialogueManager, questManager);

            // 8. Scene & Theme Stylesheet
            Scene scene = new Scene(gameView, 1120, 820);
            if (getClass().getResource("/styles/theme.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            }

            primaryStage.setTitle("The Lost Facility — Tactical Mystery RPG");
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(720);
            primaryStage.setScene(scene);

            primaryStage.setOnCloseRequest(e -> {
                if (audioService != null) {
                    audioService.shutdown();
                }
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (audioService != null) {
            audioService.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
