package dev.ua.ikeepcalm.underwhale.game;

import com.almasb.fxgl.dsl.FXGL;
import dev.ua.ikeepcalm.underwhale.Underwhale;
import dev.ua.ikeepcalm.underwhale.progress.StoryProgress;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public class TwentyOne {

    private int playerScore = 0;
    private int botScore = 0;

    private boolean isPlayerTurn = true;
    private boolean playerStopped = false;
    private boolean botStopped = false;

    private final Text playerScoreText;
    private final Text botScoreText;

    private final ImageView playerDice;
    private final ImageView botDice;

    public TwentyOne(Text playerScoreText, Text botScoreText, ImageView playerDice, ImageView botDice) {
        this.playerScoreText = playerScoreText;
        this.botScoreText = botScoreText;
        this.playerDice = playerDice;
        this.botDice = botDice;
    }

    private void animateDiceRoll(boolean isPlayerTurn, Consumer<Integer> onRollComplete) {
        Timeline timeline = new Timeline();
        int[] currentFace = {1};

        int randomFace = (int) (Math.random() * 6) + 1;
        final int[] animatingFace = {1};

        int duration = isPlayerTurn ? 100 : 150;
        KeyFrame frame = new KeyFrame(Duration.millis(duration), event -> {
            if (animatingFace[0] < 6) {
                animatingFace[0]++;
            } else {
                animatingFace[0] = 1;
            }

            ImageView dice = isPlayerTurn ? playerDice : botDice;
            dice.setImage(FXGL.texture((isPlayerTurn ? "player" : "bot") + "/animation/ani" + animatingFace[0] + ".png").getImage());
            dice.setRotate(dice.getRotate() + 40);
            currentFace[0] = randomFace;
        });

        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(30);
        timeline.setOnFinished(event -> {
            int finalRoll = currentFace[0];
            ImageView dice = isPlayerTurn ? playerDice : botDice;
            dice.setImage(FXGL.texture((isPlayerTurn ? "player" : "bot") + "/dice" + currentFace[0] + ".png").getImage());
            dice.setRotate(0);
            onRollComplete.accept(finalRoll);
        });

        timeline.play();
    }

    public void playerTurn() {
        if (botStopped && playerStopped) {
            endGame();
            return;
        }

        animateDiceRoll(true, this::handleRoll);
    }

    public void handleRoll(int playerRoll) {
        playerScoreText.setText("Player: " + playerScore + " (+ " + playerRoll + ")");
        isPlayerTurn = false;

        FXGL.runOnce(() -> {
            playerScore += playerRoll;
            playerScoreText.setText("Player: " + playerScore);
            if (playerScore >= 21) {
                endGame();
            } else {
                botTurn();
            }
        }, Duration.seconds(2));
    }

    private String getLineByEstimatedScore(int estimatedScore) {
        if (estimatedScore > 18) {
            return "Haha! I'm feeling lucky! Not my day to lose!";
        } else if (estimatedScore > 15) {
            return "I think today is my lucky day! Let's roll!";
        } else if (estimatedScore > 12) {
            return "Did you know my lucky number is " + estimatedScore + "? Me neither! Let's roll!";
        } else if (estimatedScore > 9) {
            return "Hmm... I'm not sure about this one. But who cares, we WIN THOSE!";
        } else if (estimatedScore > 6) {
            return "That's.... that's not right. I have no choice, it is about me winning or losing!";
        }
        return "I'm not feeling lucky this time! I'll stop here!";
    }

    public void botTurn() {
        if (botStopped) {
            endGame();
            return;
        }

        int riskyLimit = 4;
        int estimatedScore = minimax(botScore, playerScore, true, 3);

        if (estimatedScore > 4) {
            // Add Flowey's commentary as a non-blocking auto-advance dialog
            Underwhale.getInstance().showDialogLines(
                    List.of(getLineByEstimatedScore(estimatedScore)),
                    null, // No next story progress
                    true // Auto-advance
            );
        }

        boolean shouldRoll = estimatedScore > riskyLimit;

        if (shouldRoll) {
            animateDiceRoll(false, this::handleBotRoll);
        } else {
            showStyledDialog("Bot stopped! Player can continue rolling", StoryProgress.THE_GAME);
            isPlayerTurn = true;
            botStopped = true;

            if (playerStopped) {
                endGame();
            }
        }
    }


    private void handleBotRoll(int botRoll) {
        botScore += botRoll;
        botScoreText.setText("Bot: " + botScore);

        if (botScore >= 21) {
            endGame();
        } else {
            if (playerStopped) {
                FXGL.runOnce(this::botTurn, Duration.seconds(2));
            } else {
                isPlayerTurn = true;
                Underwhale.getInstance().recursiveStoryLoop(StoryProgress.THE_GAME);
            }
        }
    }

    private int minimax(int botScore, int playerScore, boolean isBotTurn, int depth) {
        if (botScore > 21) return -100;
        if (playerScore > 21) return 100;
        if (botStopped && playerStopped) {
            return Integer.compare(21 - botScore, 21 - playerScore);
        }
        if (depth == 0) {
            return 21 - botScore;
        }

        if (isBotTurn) {
            int maxScore = Integer.MIN_VALUE;

            for (int roll = 1; roll <= 6; roll++) {
                int newBotScore = botScore + roll;
                int score = minimax(newBotScore, playerScore, false, depth - 1);
                maxScore = Math.max(maxScore, score);
            }

            botStopped = true;
            int stopScore = minimax(botScore, playerScore, false, depth - 1);
            botStopped = false;

            return Math.max(maxScore, stopScore);
        } else {
            int minScore = Integer.MAX_VALUE;

            for (int roll = 1; roll <= 6; roll++) {
                int newPlayerScore = playerScore + roll;
                int score = minimax(botScore, newPlayerScore, true, depth - 1);
                minScore = Math.min(minScore, score);
            }

            int stopScore = minimax(botScore, playerScore, true, depth - 1);

            return Math.min(minScore, stopScore);
        }
    }

    public void handleStop() {
        if (isPlayerTurn) {
            playerStopped = true;
            isPlayerTurn = false;

            showStyledDialog("Player stopped! Bot will continue rolling", StoryProgress.THE_GAME);
            botTurn();
        }
    }

    private void endGame() {
        if (playerScore > 21) {
            showStyledDialog("Game Over: Bot Wins! Player Busted", StoryProgress.ENDING);
            return;
        } else if (botScore > 21) {
            showStyledDialog("Game Over: Player Wins! Bot Busted", StoryProgress.ENDING);
            return;
        }

        if (playerStopped && botStopped) {
            String result;
            if (playerScore > botScore) {
                result = "Player Wins!";
            } else if (botScore > playerScore) {
                result = "Bot Wins!";
            } else {
                result = "It's a Tie!";
            }

            showStyledDialog("Game Over: " + result, StoryProgress.ENDING);
            return;
        }

        // If we reach here, no immediate game end. Check for direct 21 conditions
        if (playerScore == 21) {
            showStyledDialog("Game Over: Player Wins! Player reached 21", StoryProgress.ENDING);
            return;
        }

        if (botScore == 21) {
            showStyledDialog("Game Over: Bot Wins! Bot reached 21", StoryProgress.ENDING);
            return;
        }
    }

    private void showStyledDialog(String message, StoryProgress storyProgress) {
        Underwhale.getInstance().showDialogLines(List.of(message), storyProgress, false);
    }

    public boolean isPlayerStopped() {
        return playerStopped;
    }

}
