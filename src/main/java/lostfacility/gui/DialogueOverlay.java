package lostfacility.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lostfacility.system.DialogueChoice;
import lostfacility.system.DialogueNode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Retro cyberpunk modal dialogue overlay rendering speaker details,
 * dialogue text, and numbered response choices.
 */
public class DialogueOverlay extends VBox {

    private final Label speakerLabel;
    private final Label messageLabel;
    private final VBox choicesContainer;
    private Consumer<Integer> onChoiceSelected;
    private boolean active = false;

    public DialogueOverlay() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(12);
        setPadding(new Insets(16, 20, 16, 20));
        setMaxWidth(680);
        setVisible(false);
        setManaged(false);

        // Cyberpunk styling
        setStyle("""
            -fx-background-color: rgba(8, 14, 24, 0.94);
            -fx-border-color: #00f0ff;
            -fx-border-width: 2px;
            -fx-border-radius: 6px;
            -fx-background-radius: 6px;
            -fx-effect: dropshadow(three-pass-box, rgba(0, 240, 255, 0.35), 14, 0, 0, 0);
        """);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("◈");
        icon.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        icon.setTextFill(Color.web("#00f0ff"));

        speakerLabel = new Label("COMMS LINK ACTIVE");
        speakerLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        speakerLabel.setTextFill(Color.web("#38bdf8"));

        header.getChildren().addAll(icon, speakerLabel);

        // Dialogue Message Text
        messageLabel = new Label();
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#f1f5f9"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(640);
        messageLabel.setLineSpacing(3);

        // Choices Container
        choicesContainer = new VBox(8);
        choicesContainer.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, messageLabel, choicesContainer);
    }

    public boolean isActive() {
        return active;
    }

    public void showDialogue(DialogueNode node, List<DialogueChoice> choices, Consumer<Integer> choiceCallback) {
        if (node == null) {
            hideDialogue();
            return;
        }

        this.onChoiceSelected = choiceCallback;
        speakerLabel.setText("[" + node.getSpeaker().toUpperCase() + "]");
        messageLabel.setText(node.getText());

        choicesContainer.getChildren().clear();

        for (int i = 0; i < choices.size(); i++) {
            final int index = i;
            DialogueChoice choice = choices.get(i);

            Button choiceBtn = new Button((index + 1) + ". " + choice.text());
            choiceBtn.setFont(Font.font("Monospaced", FontWeight.NORMAL, 13));
            choiceBtn.setMaxWidth(Double.MAX_VALUE);
            choiceBtn.setAlignment(Pos.BASELINE_LEFT);
            HBox.setHgrow(choiceBtn, Priority.ALWAYS);

            choiceBtn.setStyle("""
                -fx-background-color: #162032;
                -fx-text-fill: #38bdf8;
                -fx-border-color: #334155;
                -fx-border-width: 1px;
                -fx-border-radius: 4px;
                -fx-background-radius: 4px;
                -fx-padding: 6 12 6 12;
                -fx-cursor: hand;
            """);

            choiceBtn.setOnMouseEntered(e -> choiceBtn.setStyle("""
                -fx-background-color: #1e293b;
                -fx-text-fill: #00f0ff;
                -fx-border-color: #00f0ff;
                -fx-border-width: 1px;
                -fx-border-radius: 4px;
                -fx-background-radius: 4px;
                -fx-padding: 6 12 6 12;
                -fx-cursor: hand;
            """));

            choiceBtn.setOnMouseExited(e -> choiceBtn.setStyle("""
                -fx-background-color: #162032;
                -fx-text-fill: #38bdf8;
                -fx-border-color: #334155;
                -fx-border-width: 1px;
                -fx-border-radius: 4px;
                -fx-background-radius: 4px;
                -fx-padding: 6 12 6 12;
                -fx-cursor: hand;
            """));

            choiceBtn.setOnAction(e -> {
                if (onChoiceSelected != null) {
                    onChoiceSelected.accept(index);
                }
            });

            choicesContainer.getChildren().add(choiceBtn);
        }

        setVisible(true);
        setManaged(true);
        active = true;
    }

    public void hideDialogue() {
        setVisible(false);
        setManaged(false);
        active = false;
        choicesContainer.getChildren().clear();
    }

    public void selectChoiceByIndex(int oneBasedIndex) {
        int zeroBased = oneBasedIndex - 1;
        if (active && onChoiceSelected != null && zeroBased >= 0 && zeroBased < choicesContainer.getChildren().size()) {
            onChoiceSelected.accept(zeroBased);
        }
    }
}
