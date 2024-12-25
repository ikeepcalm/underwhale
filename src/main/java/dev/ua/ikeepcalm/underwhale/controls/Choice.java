package dev.ua.ikeepcalm.underwhale.controls;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class Choice {

    private Pane choicePane;
    private List<String> choices;
    private int currentChoiceIndex = 0;
    private Consumer<Integer> onChoiceSelected;

    private Text[] choiceTexts;

    public Choice(Pane choicePane) {
        this.choicePane = choicePane;
    }

    public void displayChoices(List<String> choices, Consumer<Integer> onChoiceSelected) {
        this.choices = choices;
        this.onChoiceSelected = onChoiceSelected;

        choicePane.setVisible(true);
        choicePane.getChildren().clear();

        choiceTexts = new Text[choices.size()];
        for (int i = 0; i < choices.size(); i++) {
            Text choiceText = new Text(choices.get(i));
            choiceText.setFont(FXGL.getAssetLoader().loadFont("undertale.ttf").newFont(96));
            choiceText.setLayoutX(100);
            choiceText.setLayoutY(100 + i * 40);
            choiceText.setStyle("-fx-font-size: 40; -fx-fill: white;");
            choicePane.getChildren().add(choiceText);
            choiceTexts[i] = choiceText;
        }

        highlightChoice(0);
    }

    private void highlightChoice(int index) {
        for (int i = 0; i < choiceTexts.length; i++) {
            if (i == index) {
                choiceTexts[i].setStyle("-fx-font-size: 48; -fx-fill: yellow;");
            } else {
                choiceTexts[i].setStyle("-fx-font-size: 40; -fx-fill: white;");
            }
        }
    }

    public void moveUp() {
        if (currentChoiceIndex > 0) {
            currentChoiceIndex--;
            highlightChoice(currentChoiceIndex);
        }
    }

    public void moveDown() {
        if (currentChoiceIndex < choices.size() - 1) {
            currentChoiceIndex++;
            highlightChoice(currentChoiceIndex);
        }
    }

    public void confirmChoice() {
        if (onChoiceSelected != null) {
            onChoiceSelected.accept(currentChoiceIndex);
            choicePane.getChildren().clear();
            choicePane.setVisible(false);
        }
    }
}
