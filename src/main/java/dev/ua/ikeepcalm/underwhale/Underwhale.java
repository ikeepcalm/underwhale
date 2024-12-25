package dev.ua.ikeepcalm.underwhale;

import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.UserAction;
import dev.ua.ikeepcalm.underwhale.controls.Choice;
import dev.ua.ikeepcalm.underwhale.controls.Dialog;
import dev.ua.ikeepcalm.underwhale.game.TwentyOne;
import dev.ua.ikeepcalm.underwhale.progress.StoryProgress;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Underwhale extends GameApplication {

    Button startButton;

    Text dialogText;
    Pane dialogPane;
    Pane choicePane;

    Pane gamePane;

    ImageView introThumbnail;
    ImageView bossImageView;

    Text playerScoreText;
    Text botScoreText;
    ImageView playerDice;
    ImageView botDice;

    public static Underwhale instance;

    private Dialog dialogController;
    private Choice choiceController;
    private TwentyOne twentyOne;

    private final Queue<DialogRequest> dialogQueue = new LinkedList<>();

    private boolean isDialogInProgress = false;
    private boolean isGameRunning = false;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1920);
        settings.setHeight(1080);
        settings.setFullScreenFromStart(true);
        settings.setFullScreenAllowed(true);
        settings.setTitle("Underwhale");
        settings.setFontUI("undertale.ttf");
        settings.setVersion("0.1");
        settings.setDefaultCursor(new CursorInfo("cursor.png", 0, 0));
    }

    @Override
    protected void initUI() {
        FXGL.getGameScene().getRoot().getStylesheets().add("assets/ui/css/style.css");

        introThumbnail = new ImageView(new Image("assets/textures/underwhale.png", 1920, 1080, false, true));
        introThumbnail.setLayoutX(0);
        introThumbnail.setLayoutY(0);
        FXGL.getGameScene().addUINode(introThumbnail);

        dialogPane = new Pane();
        dialogPane.setPrefWidth(1800);
        dialogPane.setPrefHeight(300);
        dialogPane.setLayoutX(50);
        dialogPane.setLayoutY(750);
        dialogPane.getStyleClass().add("dialog-pane");
        dialogPane.setVisible(false);

        choicePane = new Pane();
        choicePane.setPrefWidth(1800);
        choicePane.setPrefHeight(300);
        choicePane.setLayoutX(50);
        choicePane.setLayoutY(750);
        choicePane.getStyleClass().add("dialog-pane");
        choicePane.setVisible(false);

        gamePane = new Pane();
        gamePane.setPrefWidth(500);
        gamePane.setPrefHeight(300);
        gamePane.setLayoutX(50);
        gamePane.setLayoutY(350);
        gamePane.getStyleClass().add("dialog-pane");
        gamePane.setVisible(false);
        FXGL.getGameScene().addUINode(gamePane);

        playerScoreText = new Text("Player: 0");
        playerScoreText.getStyleClass().add("score-text");
        playerScoreText.setFont(FXGL.getAssetLoader().loadFont("undertale.ttf").newFont(36));
        playerScoreText.setLayoutX(50);
        playerScoreText.setLayoutY(50);
        playerScoreText.setVisible(false);
        gamePane.getChildren().add(playerScoreText);

        botScoreText = new Text("Bot: 0");
        botScoreText.getStyleClass().add("score-text");
        botScoreText.setFont(FXGL.getAssetLoader().loadFont("undertale.ttf").newFont(36));
        botScoreText.setLayoutX(350);
        botScoreText.setLayoutY(50);
        botScoreText.setVisible(false);
        gamePane.getChildren().add(botScoreText);

        playerDice = new ImageView(FXGL.getAssetLoader().loadTexture("player/dice1.png").getImage());
        playerDice.setLayoutX(80);
        playerDice.setLayoutY(500);
        playerDice.setVisible(false);
        FXGL.getGameScene().addUINode(playerDice);

        botDice = new ImageView(FXGL.getAssetLoader().loadTexture("bot/dice1.png").getImage());
        botDice.setLayoutX(400);
        botDice.setLayoutY(500);
        botDice.setVisible(false);
        FXGL.getGameScene().addUINode(botDice);

        dialogText = new Text("Hello, human!");
        dialogText.getStyleClass().add("dialog-text");
        dialogText.setFont(FXGL.getAssetLoader().loadFont("undertale.ttf").newFont(150));
        dialogText.setLayoutX(50);
        dialogText.setLayoutY(50);
        dialogPane.getChildren().add(dialogText);

        bossImageView = new ImageView(new Image("assets/textures/boss/idle.png", 512, 512, true, true));
        bossImageView.setLayoutX(700);
        bossImageView.setLayoutY(100);
        bossImageView.setVisible(false);

        startButton = new Button("New Game");
        startButton.setFont(FXGL.getAssetLoader().loadFont("undertale.ttf").newFont(48));
        startButton.setPrefHeight(100);
        startButton.setPrefWidth(200);
        startButton.setLayoutX(FXGL.getAppWidth() / 2.0 - 70);
        startButton.setLayoutY(FXGL.getAppHeight() / 2.0 + 150);
        startButton.getStyleClass().add("start-button");
        startButton.setOnAction(e -> {
            introThumbnail.setVisible(false);
            FXGL.getGameScene().removeUINode(startButton);
            dialogPane.setVisible(true);
            bossImageView.setVisible(true);
            FXGL.getAudioPlayer().stopAllMusic();
            Sound enter = FXGL.getAssetLoader().loadSound("enter.mp3");
            FXGL.getAudioPlayer().playSound(enter);
            FXGL.getAudioPlayer().loopMusic(FXGL.getAssetLoader().loadMusic("guide.mp3"));

            recursiveStoryLoop(StoryProgress.DIALOG);
        });

        FXGL.getSettings().setGlobalMusicVolume(0.05);
        FXGL.getSettings().setGlobalSoundVolume(0.05);
        Music music = FXGL.getAssetLoader().loadMusic("intro.mp3");
        FXGL.getAudioPlayer().playMusic(music);

        FXGL.getGameScene().addUINode(startButton);
        FXGL.getGameScene().addUINode(bossImageView);
        FXGL.getGameScene().addUINode(dialogPane);
        FXGL.getGameScene().addUINode(choicePane);
    }

    @Override
    protected void initGame() {
        instance = this;
    }

    public void recursiveStoryLoop(StoryProgress progress) {
        switch (progress) {
            case DIALOG -> {
                List<String> dialogLines = List.of(
                        "Heh heh heh... So you've come at last, human.",
                        "Welcome to my domain, the Underwhale!",
                        "Let's see if you're ready to face me in a battle of wits and dice.",
                        "Until now my skibidi rizz power was not beat once!"
                );
                showDialogLines(dialogLines, StoryProgress.DIALOG_2, false);
            }

            case DIALOG_2 -> {
                List<String> dialogLines = List.of(
                        "Have you ever heard about the game of Twenty-One?",
                        "No? Such a shame. It's a game of chance and strategy.",
                        "The rules are simple: roll dice and try to get as close to 21 as possible.",
                        "But be careful! If you go over 21, you lose.",
                        "Are you ready to play?"
                );
                showDialogLines(dialogLines, StoryProgress.BOSS_FIGHT, false);
            }

            case BOSS_FIGHT -> {
                makeFloweyTalk(false);
                choiceController = new Choice(choicePane);
                List<String> choices = List.of("Yes", "No");
                choicePane.setVisible(true);
                choiceController.displayChoices(choices, choice -> {
                    choicePane.setVisible(false);
                    FXGL.getAudioPlayer().stopAllMusic();
                    FXGL.getAudioPlayer().loopMusic(FXGL.getAssetLoader().loadMusic("megalovania.mp3"));
                    if (choice == 0) {
                        showDialogLines(List.of("Excellent! Let's begin!", "Roll the dice!"), StoryProgress.THE_GAME, false);
                    } else {
                        List<String> dialogLines = List.of(
                                "Oh, what a pity. Maybe next time.",
                                "SIKE! You don't have a choice! Let's play!",
                                "Roll the dice!"
                        );
                        showDialogLines(dialogLines, StoryProgress.THE_GAME, false);
                    }
                });
            }

            case THE_GAME -> {
                if (!isGameRunning) {
                    playerScoreText.setVisible(true);
                    botScoreText.setVisible(true);
                    playerDice.setVisible(true);
                    botDice.setVisible(true);
                    gamePane.setVisible(true);
                    twentyOne = new TwentyOne(playerScoreText, botScoreText, playerDice, botDice);
                    isGameRunning = true;
                }

                if (!twentyOne.isPlayerStopped()) {
                    makeFloweyTalk(false);
                    choicePane.setVisible(true);
                    choiceController = new Choice(choicePane);
                    List<String> choices = List.of("Roll", "Stop");
                    choiceController.displayChoices(choices, choice -> {
                        choicePane.setVisible(false);
                        if (choice == 0) {
                            twentyOne.playerTurn();
                        } else {
                            twentyOne.handleStop();
                        }
                    });
                } else {
                    dialogText.setText("");
                }
            }

            case ENDING -> {
                FXGL.getAudioPlayer().stopAllMusic();
                FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound("enter.mp3"));
                List<String> dialogLines = List.of(
                        "Congratulations! You've defeated me!",
                        "I'll be back!",
                        "Thanks for playing!"
                );
                showDialogLines(dialogLines, StoryProgress.INTRO, true);
            }

            case INTRO -> {
                for (int i = 1; i <= 5; i++) {
                    int finalI = i;
                    FXGL.runOnce(() -> bossImageView.setImage(new Image("assets/textures/boss/sink" + finalI + ".png", 512, 512, true, true)), Duration.seconds(finalI * 0.5));
                    if (i == 5) {
                        FXGL.runOnce(() -> {
                            bossImageView.setVisible(false);
                        }, Duration.seconds(finalI * 0.5 + 1));
                    }
                }

                FXGL.runOnce(() -> {
                    FXGL.showConfirm("Congratulations!\nDo you want to play again?", yes -> {
                        if (yes) {
                            recursiveStoryLoop(StoryProgress.DIALOG);
                            twentyOne = null;
                            isGameRunning = false;
                            playerScoreText.setVisible(false);
                            botScoreText.setVisible(false);
                            playerDice.setVisible(false);
                            botDice.setVisible(false);
                            gamePane.setVisible(false);
                            choicePane.setVisible(false);
                            bossImageView.setVisible(true);
                            dialogText.setText("");
                            dialogController = null;
                            choiceController = null;
                            isDialogInProgress = false;
                            twentyOne = null;
                            isGameRunning = false;
                            playerScoreText.setText("Player: 0");
                            botScoreText.setText("Bot: 0");
                            playerDice.setVisible(false);
                            botDice.setVisible(false);
                        } else {
                            FXGL.getGameController().exit();
                        }
                    });
                }, Duration.seconds(6));
            }
        }
    }

    public void showDialogLines(List<String> dialogLines, StoryProgress nextStoryProgress, boolean autoAdvance) {
        dialogQueue.add(new DialogRequest(dialogLines, nextStoryProgress, autoAdvance));

        if (!isDialogInProgress) {
            processNextDialog();
        }
    }

    private void processNextDialog() {
        if (dialogQueue.isEmpty()) {
            return;
        }

        isDialogInProgress = true;
        DialogRequest request = dialogQueue.poll();

        makeFloweyTalk(true);
        dialogController = new Dialog(dialogText)
                .startDialog(request.dialogLines, request.autoAdvance, () -> {
                    isDialogInProgress = false;
                    makeFloweyTalk(false);

                    if (request.nextStoryProgress != null) {
                        recursiveStoryLoop(request.nextStoryProgress);
                    }

                    processNextDialog();
                });
    }

    private record DialogRequest(List<String> dialogLines, StoryProgress nextStoryProgress, boolean autoAdvance) {
    }

    public void makeFloweyTalk(boolean talking) {
        if (talking) {
            bossImageView.setImage(new Image("assets/textures/boss/talking.png", 512, 512, true, true));
        } else {
            bossImageView.setImage(new Image("assets/textures/boss/idle.png", 512, 512, true, true));
        }
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addAction(new UserAction("Next Dialog") {
            @Override
            protected void onActionBegin() {
                if (dialogController != null) {
                    dialogController.onNext();
                }
            }
        }, KeyCode.SPACE);

        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                if (choiceController != null) {
                    choiceController.moveDown();
                }
            }
        }, KeyCode.DOWN);

        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                if (choiceController != null) {
                    choiceController.moveUp();
                }
            }
        }, KeyCode.UP);

        FXGL.getInput().addAction(new UserAction("Confirm") {
            @Override
            protected void onActionBegin() {
                if (choiceController != null) {
                    choiceController.confirmChoice();
                }
            }
        }, KeyCode.ENTER);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Underwhale getInstance() {
        return instance;
    }

}
