package dev.ua.ikeepcalm.underwhale.controls;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

public class Dialog {

    private final Text dialogText;
    private List<String> dialogLines;
    private int currentLineIndex = 0;

    private boolean isTyping = false;
    private boolean autoAdvance = false;
    private String currentLine = "";
    private int currentCharIndex = 0;

    private Runnable onDialogComplete;

    public Dialog(Text dialogText) {
        this.dialogText = dialogText;
    }

    public Dialog startDialog(List<String> dialogLines, boolean autoAdvance, Runnable onDialogComplete) {
        this.dialogLines = dialogLines;
        this.autoAdvance = autoAdvance;
        this.onDialogComplete = onDialogComplete;
        this.currentLineIndex = 0;

        if (!dialogLines.isEmpty()) {
            displayNextLine();
        } else {
            endDialog();
        }

        return this;
    }

    private void displayNextLine() {
        if (currentLineIndex < dialogLines.size()) {
            currentLine = dialogLines.get(currentLineIndex);
            currentCharIndex = 0;
            dialogText.setText("");
            isTyping = true;

            FXGL.getGameTimer().runAtInterval(() -> {
                if (currentCharIndex < currentLine.length() && isTyping) {
                    dialogText.setText(dialogText.getText() + currentLine.charAt(currentCharIndex));
                    currentCharIndex++;
                } else if (isTyping) {
                    isTyping = false;

                    if (autoAdvance) {
                        FXGL.runOnce(this::onNext, Duration.seconds(1));
                    }
                }
            }, Duration.millis(50));

            FXGL.getGameTimer().runAtInterval(() -> {
                if (isTyping) {
                    FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound("talking.wav"));
                }
            }, Duration.millis(200));

            currentLineIndex++;
        } else {
            endDialog();
        }
    }

    public void onNext() {
        if (isTyping) {
            dialogText.setText(currentLine);
            isTyping = false;
        } else if (currentLineIndex < dialogLines.size()) {
            displayNextLine();
        } else {
            endDialog();
        }
    }

    private void endDialog() {
        dialogText.setText("");

        if (onDialogComplete != null) {
            onDialogComplete.run();
        }
    }
}
