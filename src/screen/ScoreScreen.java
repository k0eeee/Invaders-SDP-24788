package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import engine.Cooldown;
import engine.Core;
import engine.GameState;
import engine.Score;

/**
 * Implements the score screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class ScoreScreen extends Screen {

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;
    /** Maximum number of high scores. */
    private static final int MAX_HIGH_SCORE_NUM = 7;
    /** Code of first mayus character. */
    private static final int FIRST_CHAR = 65;
    /** Code of last mayus character. */
    private static final int LAST_CHAR = 90;

    /** Current score player 1. */
    private int score1;
    /** Current score player 2. */
    private int score2;

    /** Player 1 lives left. */
    private int livesRemaining1;
    /** Player 2 lives left. */
    private int livesRemaining2;

    /** Total bullets shot by the player 1. */
    private int bulletsShot1;
    /** Total bullets shot by the player 2. */
    private int bulletsShot2;

    /** Total ships destroyed by the player 1. */
    private int shipsDestroyed1;
    /** Total ships destroyed by the player 2. */
    private int shipsDestroyed2;

    /** List of past high scores. */
    private List<Score> highScores;
    /** Checks if current score is a new high score. */
    private boolean isNewRecord;
    /** Player name for record input. */
    private char[] name;
    /** Character of players name selected for change. */
    private int nameCharSelected;
    /** Time between changes in user selection. */
    private Cooldown selectionCooldown;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     * @param gameState
     *            Current game state.
     */
    public ScoreScreen(final int width, final int height, final int fps,
                       final GameState gameState) {
        super(width, height, fps);

        // Player 1 Data
        this.score1 = gameState.getScore();
        this.livesRemaining1 = gameState.getLivesRemaining();
        this.bulletsShot1 = gameState.getBulletsShot();
        this.shipsDestroyed1 = gameState.getShipsDestroyed();

        // Player 2 Data
        this.score2 = gameState.getScore2();
        this.livesRemaining2 = gameState.getLivesRemaining2();
        this.bulletsShot2 = gameState.getBulletsShot2();
        this.shipsDestroyed2 = gameState.getShipsDestroyed2();

        this.isNewRecord = false;
        this.name = "AAA".toCharArray();
        this.nameCharSelected = 0;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();

        try {
            this.highScores = Core.getFileManager().loadHighScores();
            if (highScores.size() < MAX_HIGH_SCORE_NUM
                    || highScores.get(highScores.size() - 1).getScore()
                    < this.score1) // Por ahora solo valida jugador1
                this.isNewRecord = true;

        } catch (IOException e) {
            logger.warning("Couldn't load high scores!");
        }
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        draw();
        if (this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                // Return to main menu.
                this.returnCode = 1;
                this.isRunning = false;
                if (this.isNewRecord)
                    saveScore();
            } else if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                // Play again.
                this.returnCode = 2;
                this.isRunning = false;
                if (this.isNewRecord)
                    saveScore();
            }

            if (this.isNewRecord && this.selectionCooldown.checkFinished()) {
                if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
                    this.nameCharSelected = this.nameCharSelected == 2 ? 0
                            : this.nameCharSelected + 1;
                    this.selectionCooldown.reset();
                }
                if (inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
                    this.nameCharSelected = this.nameCharSelected == 0 ? 2
                            : this.nameCharSelected - 1;
                    this.selectionCooldown.reset();
                }
                if (inputManager.isKeyDown(KeyEvent.VK_UP)) {
                    this.name[this.nameCharSelected] =
                            (char) (this.name[this.nameCharSelected]
                                    == LAST_CHAR ? FIRST_CHAR
                                    : this.name[this.nameCharSelected] + 1);
                    this.selectionCooldown.reset();
                }
                if (inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
                    this.name[this.nameCharSelected] =
                            (char) (this.name[this.nameCharSelected]
                                    == FIRST_CHAR ? LAST_CHAR
                                    : this.name[this.nameCharSelected] - 1);
                    this.selectionCooldown.reset();
                }
            }
        }

    }

    /**
     * Saves the score as a high score.
     */
    private void saveScore() {
        highScores.add(new Score(new String(this.name), score1));
        Collections.sort(highScores);
        if (highScores.size() > MAX_HIGH_SCORE_NUM)
            highScores.remove(highScores.size() - 1);

        try {
            Core.getFileManager().saveHighScores(highScores);
        } catch (IOException e) {
            logger.warning("Couldn't load high scores!");
        }
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawGameOver(this, this.inputDelay.checkFinished(),
                this.isNewRecord);
        drawManager.drawResults(this, this.score1, this.livesRemaining1,
                this.shipsDestroyed1, (float) this.shipsDestroyed1
                        / this.bulletsShot1, this.score2, this.livesRemaining2,
                this.shipsDestroyed2, (float) this.shipsDestroyed2 / this.bulletsShot2, this.isNewRecord);


        if (this.isNewRecord)
            drawManager.drawNameInput(this, this.name, this.nameCharSelected);

        drawManager.completeDrawing(this);
    }
}
