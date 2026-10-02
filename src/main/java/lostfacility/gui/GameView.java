package lostfacility.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import lostfacility.action.*;
import lostfacility.engine.GameEngine;
import lostfacility.engine.GameState;
import lostfacility.event.*;
import lostfacility.model.*;
import lostfacility.persistence.SaveManager;
import lostfacility.system.*;

import java.util.List;

/**
 * Primary game view coordinating the retro sci-fi GUI:
 * Top HUD, Center GameCanvas & DialogueOverlay, Right Inventory/Quest sidebar,
 * and Bottom command console & action toolbar.
 */
public class GameView extends BorderPane {

    private final GameEngine engine;
    private final GameCanvas canvas;
    private final DialogueOverlay dialogueOverlay;
    private final AudioService audioService;
    private final DialogueManager dialogueManager;
    private final QuestManager questManager;
    private final SaveManager saveManager = new SaveManager();

    // Top HUD Controls
    private final Label roomTitleLabel = new Label();
    private final Label roomDescLabel = new Label();
    private final ProgressBar hpBar = new ProgressBar(1.0);
    private final Label hpTextLabel = new Label();
    private final Label statsLabel = new Label();
    private final Button muteButton = new Button("🔊 Sound: ON");

    // Right Sidebar Controls
    private final ListView<Item> inventoryListView = new ListView<>();
    private final Label itemDetailLabel = new Label("Select an item to view specs.");
    private final Button useItemBtn = new Button("Use");
    private final Button equipItemBtn = new Button("Equip");
    private final Button dropItemBtn = new Button("Drop");

    private final Label questTitleLabel = new Label("No Active Mission");
    private final VBox questObjectivesBox = new VBox(6);
    private final ListView<String> radarListView = new ListView<>();

    // Bottom Console Controls
    private final TextArea consoleOutput = new TextArea();
    private final TextField commandInput = new TextField();

    public GameView(GameEngine engine, GameCanvas canvas, DialogueOverlay dialogueOverlay,
                    AudioService audioService, DialogueManager dialogueManager, QuestManager questManager) {
        this.engine = engine;
        this.canvas = canvas;
        this.dialogueOverlay = dialogueOverlay;
        this.audioService = audioService;
        this.dialogueManager = dialogueManager;
        this.questManager = questManager;

        setupUI();
        wireEventSubscriptions();
        updateUI();
    }

    private void setupUI() {
        // 1. Top HUD
        setTop(buildTopHud());

        // 2. Center Viewport (StackPane with Canvas and DialogueOverlay)
        StackPane centerStack = new StackPane();
        centerStack.setAlignment(Pos.CENTER);
        centerStack.getChildren().addAll(canvas, dialogueOverlay);
        setCenter(centerStack);

        // 3. Right Sidebar
        setRight(buildRightSidebar());

        // 4. Bottom Console & Actions
        setBottom(buildBottomPanel());

        // Global Key handler
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKey);
    }

    private Node buildTopHud() {
        HBox hud = new HBox(20);
        hud.getStyleClass().add("hud-panel");
        hud.setAlignment(Pos.CENTER_LEFT);

        // Room Info (Left)
        VBox roomBox = new VBox(2);
        roomTitleLabel.getStyleClass().add("room-title");
        roomDescLabel.getStyleClass().add("room-desc");
        roomBox.setMinWidth(260);
        roomBox.getChildren().addAll(roomTitleLabel, roomDescLabel);

        // Health & Stats (Center)
        VBox statsBox = new VBox(4);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        hpBar.getStyleClass().add("hp-bar");
        hpBar.setPrefWidth(220);

        HBox hpRow = new HBox(10);
        hpRow.setAlignment(Pos.CENTER_LEFT);
        hpTextLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-text-fill: #34d399; -fx-font-weight: bold;");
        hpRow.getChildren().addAll(hpBar, hpTextLabel);

        statsLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-text-fill: #38bdf8;");
        statsBox.getChildren().addAll(hpRow, statsLabel);

        // Audio & System (Right)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        muteButton.getStyleClass().add("cyber-button");
        muteButton.setOnAction(e -> {
            audioService.toggleMute();
            muteButton.setText(audioService.isMuted() ? "🔇 Sound: OFF" : "🔊 Sound: ON");
        });

        hud.getChildren().addAll(roomBox, statsBox, spacer, muteButton);
        return hud;
    }

    private Node buildRightSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar-panel");
        sidebar.setPrefWidth(280);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // TAB 1: Inventory
        Tab invTab = new Tab("Inventory");
        VBox invBox = new VBox(8);
        invBox.setPadding(new Insets(10, 0, 0, 0));

        inventoryListView.getStyleClass().add("cyber-list");
        VBox.setVgrow(inventoryListView, Priority.ALWAYS);
        inventoryListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " [" + item.getType().getLabel() + "]");
                }
            }
        });

        inventoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                StringBuilder desc = new StringBuilder();
                desc.append(newVal.getName()).append(" (").append(newVal.getType().getLabel()).append(")\n");
                if (newVal.getBonusAttack() > 0) desc.append("ATK Bonus: +").append(newVal.getBonusAttack()).append("  ");
                if (newVal.getBonusDefense() > 0) desc.append("DEF Bonus: +").append(newVal.getBonusDefense()).append("  ");
                if (newVal.getHealAmount() > 0) desc.append("Heal: +").append(newVal.getHealAmount()).append(" HP  ");
                desc.append("\n").append(newVal.getDescription());
                itemDetailLabel.setText(desc.toString().trim());

                useItemBtn.setDisable(newVal.getType() != ItemType.CONSUMABLE);
                equipItemBtn.setDisable(newVal.getType() != ItemType.WEAPON && newVal.getType() != ItemType.ARMOR);
                dropItemBtn.setDisable(false);
            } else {
                itemDetailLabel.setText("Select an item to view specs.");
                useItemBtn.setDisable(true);
                equipItemBtn.setDisable(true);
                dropItemBtn.setDisable(true);
            }
        });

        itemDetailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");
        itemDetailLabel.setMinHeight(50);

        HBox itemBtnRow = new HBox(6);
        useItemBtn.getStyleClass().add("cyber-button");
        equipItemBtn.getStyleClass().add("cyber-button");
        dropItemBtn.getStyleClass().add("cyber-button");
        useItemBtn.setDisable(true);
        equipItemBtn.setDisable(true);
        dropItemBtn.setDisable(true);

        useItemBtn.setOnAction(e -> {
            Item item = inventoryListView.getSelectionModel().getSelectedItem();
            if (item != null) executeAction(new UseAction(item.getId()));
        });
        equipItemBtn.setOnAction(e -> {
            Item item = inventoryListView.getSelectionModel().getSelectedItem();
            if (item != null) executeAction(new EquipAction(item.getId()));
        });
        dropItemBtn.setOnAction(e -> {
            Item item = inventoryListView.getSelectionModel().getSelectedItem();
            if (item != null) executeAction(new UseAction(item.getId()));
        });

        itemBtnRow.getChildren().addAll(useItemBtn, equipItemBtn, dropItemBtn);
        invBox.getChildren().addAll(inventoryListView, itemDetailLabel, itemBtnRow);
        invTab.setContent(invBox);

        // TAB 2: Mission / Quests
        Tab questTab = new Tab("Mission");
        VBox questBox = new VBox(10);
        questBox.setPadding(new Insets(10, 0, 0, 0));

        questTitleLabel.getStyleClass().add("section-header");
        questObjectivesBox.setPadding(new Insets(6, 0, 0, 0));

        ScrollPane questScroll = new ScrollPane(questObjectivesBox);
        questScroll.setFitToWidth(true);
        questScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(questScroll, Priority.ALWAYS);

        questBox.getChildren().addAll(questTitleLabel, questScroll);
        questTab.setContent(questBox);

        // TAB 3: Radar
        Tab radarTab = new Tab("Radar");
        VBox radarBox = new VBox(6);
        radarBox.setPadding(new Insets(10, 0, 0, 0));
        radarListView.getStyleClass().add("cyber-list");
        VBox.setVgrow(radarListView, Priority.ALWAYS);
        radarBox.getChildren().add(radarListView);
        radarTab.setContent(radarBox);

        tabPane.getTabs().addAll(invTab, questTab, radarTab);
        sidebar.getChildren().add(tabPane);
        return sidebar;
    }

    private Node buildBottomPanel() {
        VBox bottom = new VBox(8);
        bottom.getStyleClass().add("bottom-panel");

        // Console Output
        consoleOutput.getStyleClass().add("log-console");
        consoleOutput.setEditable(false);
        consoleOutput.setWrapText(true);
        consoleOutput.setPrefRowCount(4);
        consoleOutput.setMaxHeight(95);

        // Command Bar
        HBox commandBar = new HBox(8);
        commandBar.setAlignment(Pos.CENTER_LEFT);

        Label prompt = new Label("SYS://");
        prompt.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-weight: bold; -fx-text-fill: #00f0ff;");

        commandInput.getStyleClass().add("command-input");
        commandInput.setPromptText("Enter command (e.g. 'move north', 'attack', 'take keycard', 'save 1', 'help')...");
        HBox.setHgrow(commandInput, Priority.ALWAYS);

        commandInput.setOnAction(e -> handleCommandSubmit());

        Button execBtn = new Button("EXEC");
        execBtn.getStyleClass().add("cyber-button");
        execBtn.setOnAction(e -> handleCommandSubmit());

        commandBar.getChildren().addAll(prompt, commandInput, execBtn);

        // Action Toolbar
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button northBtn = new Button("▲ N");
        Button southBtn = new Button("▼ S");
        Button westBtn = new Button("◄ W");
        Button eastBtn = new Button("► E");
        northBtn.getStyleClass().add("cyber-button");
        southBtn.getStyleClass().add("cyber-button");
        westBtn.getStyleClass().add("cyber-button");
        eastBtn.getStyleClass().add("cyber-button");

        northBtn.setOnAction(e -> executeAction(new MoveAction(Direction.NORTH)));
        southBtn.setOnAction(e -> executeAction(new MoveAction(Direction.SOUTH)));
        westBtn.setOnAction(e -> executeAction(new MoveAction(Direction.WEST)));
        eastBtn.setOnAction(e -> executeAction(new MoveAction(Direction.EAST)));

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);

        Button attackBtn = new Button("⚔ Attack");
        Button takeBtn = new Button("🖐 Take");
        Button talkBtn = new Button("💬 Talk");
        Button lookBtn = new Button("🔍 Look");
        attackBtn.getStyleClass().addAll("cyber-button", "action-button-danger");
        takeBtn.getStyleClass().add("cyber-button");
        talkBtn.getStyleClass().add("cyber-button");
        lookBtn.getStyleClass().add("cyber-button");

        attackBtn.setOnAction(e -> executeAction(new AttackAction()));
        takeBtn.setOnAction(e -> executeAction(new TakeAction()));
        talkBtn.setOnAction(e -> triggerTalkAction());
        lookBtn.setOnAction(e -> executeAction(new ExamineAction()));

        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);

        Button saveBtn = new Button("💾 Save");
        Button loadBtn = new Button("📂 Load");
        Button helpBtn = new Button("❓ Help");
        saveBtn.getStyleClass().add("cyber-button");
        loadBtn.getStyleClass().add("cyber-button");
        helpBtn.getStyleClass().add("cyber-button");

        saveBtn.setOnAction(e -> executeAction(new SaveAction("quicksave", saveManager)));
        loadBtn.setOnAction(e -> executeAction(new LoadAction("quicksave", saveManager, engine.getState().getWorld())));
        helpBtn.setOnAction(e -> appendLog("AVAILABLE COMMANDS: move <dir>, attack, take [item], use <item>, equip <item>, look, talk, save <slot>, load <slot>.", "#38bdf8"));

        toolbar.getChildren().addAll(
                westBtn, northBtn, southBtn, eastBtn,
                sep1,
                attackBtn, takeBtn, talkBtn, lookBtn,
                sep2,
                saveBtn, loadBtn, helpBtn
        );

        bottom.getChildren().addAll(consoleOutput, commandBar, toolbar);
        return bottom;
    }

    private void handleCommandSubmit() {
        String text = commandInput.getText();
        if (text == null || text.isBlank()) return;
        commandInput.clear();

        appendLog("> " + text, "#00f0ff");
        ActionResult result = engine.handleInput(text);
        if (!result.isSuccess() && result.message() != null) {
            appendLog(result.message(), "#ef4444");
        }
        updateUI();
    }

    public void executeAction(GameAction action) {
        if (action == null) return;
        ActionResult result = engine.execute(action);
        if (!result.isSuccess() && result.message() != null) {
            appendLog(result.message(), "#f43f5e");
        }
        updateUI();
    }

    private void triggerTalkAction() {
        if (dialogueManager.isInDialogue()) {
            return;
        }

        ActionResult result = engine.execute(new TalkAction(dialogueManager));
        if (result.isSuccess() && dialogueManager.isInDialogue()) {
            refreshDialogueOverlay();
        } else if (!result.isSuccess() && result.message() != null) {
            appendLog(result.message(), "#f43f5e");
        }
        updateUI();
    }

    private void refreshDialogueOverlay() {
        if (dialogueManager.isInDialogue()) {
            DialogueNode node = dialogueManager.getActiveNode();
            List<DialogueChoice> choices = dialogueManager.getAvailableChoices(engine.getState());
            dialogueOverlay.showDialogue(node, choices, choiceIndex -> {
                dialogueManager.chooseOption(choiceIndex, engine.getState(), engine.getEventManager());
                if (dialogueManager.isInDialogue()) {
                    refreshDialogueOverlay();
                } else {
                    dialogueOverlay.hideDialogue();
                }
                updateUI();
            });
        } else {
            dialogueOverlay.hideDialogue();
        }
    }

    private void handleGlobalKey(KeyEvent event) {
        // If command field is focused, let it receive typing
        if (commandInput.isFocused()) {
            return;
        }

        // Dialogue choices 1..9
        if (dialogueOverlay.isActive()) {
            KeyCode code = event.getCode();
            if (code.isDigitKey()) {
                int digit = Integer.parseInt(code.getName());
                dialogueOverlay.selectChoiceByIndex(digit);
                event.consume();
                return;
            } else if (code == KeyCode.ESCAPE) {
                dialogueManager.endDialogue();
                dialogueOverlay.hideDialogue();
                event.consume();
                return;
            }
        }

        KeyCode code = event.getCode();
        switch (code) {
            case W, UP -> {
                executeAction(new MoveAction(Direction.NORTH));
                event.consume();
            }
            case S, DOWN -> {
                executeAction(new MoveAction(Direction.SOUTH));
                event.consume();
            }
            case A, LEFT -> {
                executeAction(new MoveAction(Direction.WEST));
                event.consume();
            }
            case D, RIGHT -> {
                executeAction(new MoveAction(Direction.EAST));
                event.consume();
            }
            case SPACE, F -> {
                executeAction(new AttackAction());
                event.consume();
            }
            case G -> {
                executeAction(new TakeAction());
                event.consume();
            }
            case T -> {
                triggerTalkAction();
                event.consume();
            }
            case SLASH -> {
                Platform.runLater(commandInput::requestFocus);
                event.consume();
            }
        }
    }

    private void wireEventSubscriptions() {
        engine.getEventManager().subscribe(event -> {
            Platform.runLater(() -> {
                if (event instanceof MessageEvent me) {
                    String color = switch (me.channel()) {
                        case COMBAT -> "#f43f5e";
                        case DIALOGUE -> "#38bdf8";
                        case ERROR -> "#ef4444";
                        case SYSTEM -> "#34d399";
                        case NARRATIVE -> "#e2e8f0";
                        default -> "#94a3b8";
                    };
                    appendLog(me.text(), color);
                }
                updateUI();
            });
        });
    }

    public void updateUI() {
        GameState state = engine.getState();
        if (state == null) return;

        // 1. Update Room Info
        Room room = state.getCurrentRoom();
        if (room != null) {
            roomTitleLabel.setText(room.getName().toUpperCase());
            roomDescLabel.setText(room.getDescription());
        }

        // 2. Update Player Stats & HP Bar
        Player player = state.getPlayer();
        if (player != null) {
            int hp = player.getHp();
            int maxHp = player.getMaxHp();
            double ratio = (double) hp / maxHp;
            hpBar.setProgress(ratio);
            hpTextLabel.setText("HP: " + hp + "/" + maxHp);

            statsLabel.setText(String.format("ATK: %d | DEF: %d | LVL: %d | EXP: %d",
                    player.getEffectiveAttack(), player.getEffectiveDefense(),
                    player.getLevel(), player.getExperience()));
        }

        // 3. Update Inventory List
        if (player != null) {
            Item selected = inventoryListView.getSelectionModel().getSelectedItem();
            inventoryListView.getItems().setAll(player.getInventory().getItems());
            if (selected != null && player.getInventory().hasItem(selected.getId())) {
                inventoryListView.getSelectionModel().select(selected);
            }
        }

        // 4. Update Quest Panel
        if (questManager != null) {
            Quest activeQuest = questManager.getActiveQuest();
            if (activeQuest != null) {
                questTitleLabel.setText(activeQuest.getTitle().toUpperCase() + " [" + activeQuest.getState() + "]");
                questObjectivesBox.getChildren().clear();

                for (QuestObjective obj : activeQuest.getObjectives()) {
                    CheckBox cb = new CheckBox(obj.getDescription());
                    cb.setSelected(obj.isCompleted());
                    cb.setDisable(true);
                    cb.setStyle(obj.isCompleted()
                            ? "-fx-text-fill: #34d399; -fx-opacity: 0.9;"
                            : "-fx-text-fill: #94a3b8;");
                    questObjectivesBox.getChildren().add(cb);
                }
            } else {
                questTitleLabel.setText("NO ACTIVE MISSION");
                questObjectivesBox.getChildren().clear();
            }
        }

        // 5. Update Radar / Room Entities
        radarListView.getItems().clear();
        if (room != null) {
            for (Enemy enemy : state.getEnemiesInCurrentRoom()) {
                radarListView.getItems().add((enemy.isAlive() ? "⚠ [HOSTILE] " : "✝ [DEFEATED] ") + enemy.getName() + " (" + enemy.getHp() + "/" + enemy.getMaxHp() + " HP)");
            }
            for (Npc npc : state.getNpcsInCurrentRoom()) {
                radarListView.getItems().add("◈ [FRIENDLY] " + npc.getName());
            }
            for (int y = 0; y < room.getHeight(); y++) {
                for (int x = 0; x < room.getWidth(); x++) {
                    Tile tile = room.getTile(x, y);
                    if (tile != null && tile.hasItems()) {
                        for (Item it : tile.getItemsOnGround()) {
                            radarListView.getItems().add("★ [ITEM] " + it.getName() + " at (" + x + "," + y + ")");
                        }
                    }
                }
            }
        }

        // 6. Refresh Dialogue Overlay state
        if (dialogueManager.isInDialogue()) {
            refreshDialogueOverlay();
        } else if (dialogueOverlay.isActive()) {
            dialogueOverlay.hideDialogue();
        }

        // 7. Request Canvas redraw
        canvas.render();
    }

    private void appendLog(String message, String colorHex) {
        if (message == null || message.isBlank()) return;
        consoleOutput.appendText(message + "\n");
    }
}
