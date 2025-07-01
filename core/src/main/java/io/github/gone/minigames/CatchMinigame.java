package io.github.gone.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.gone.utils.ShapeRendererManager;

/**
 * A fishing minigame similar to Stardew Valley where the player must keep
 * a green bar aligned with a moving fish to catch it successfully.
 */
public class CatchMinigame {
    // Constants
    private static final float BAR_WIDTH = 60f;
    private static final float BAR_HEIGHT = 300f;
    private static final float GREEN_BAR_HEIGHT = 80f;
    private static final float FISH_SIZE = 30f;
    private static final float BORDER_WIDTH = 3f;

    // Physics constants
    private static final float GRAVITY = 500f;
    private static final float LIFT_FORCE = 500f;
    private static final float DRAG = 0.8f;
    private static final float FISH_SPEED_BASE = 50f;
    private static final float FISH_SPEED_VARIANCE = 30f;

    // Game duration and progress
    private static final float GAME_DURATION = 15f; // seconds
    private static final float PROGRESS_RATE_IN_ZONE = 0.15f; // per second
    private static final float PROGRESS_RATE_OUT_ZONE = -0.1f; // per second

    // Position
    private final float barX;
    private final float barY;

    // Game state
    private boolean isActive;
    private float gameTimer;
    private float progress; // 0.0 to 1.0
    private FishingResult result;
    private float resultMessageTimer;
    private static final float RESULT_MESSAGE_DURATION = 2f;

    // Green bar (player controlled)
    private float greenBarY;
    private float greenBarVelocity;
    private boolean isPressed;

    // Fish (AI controlled)
    private float fishY;
    private float fishTargetY;
    private float fishDirection;
    private float fishSpeed;
    private float fishChangeTimer;
    private static final float FISH_CHANGE_INTERVAL = 2f;

    // Fish difficulty (affects movement pattern)
    private FishDifficulty difficulty;

    // Define fish difficulty levels
    public enum FishDifficulty {
        EASY(0.5f, 1.0f, "Easy Fish"),
        MEDIUM(0.8f, 1.5f, "Medium Fish"),
        HARD(1.2f, 2.0f, "Hard Fish"),
        LEGENDARY(1.8f, 3.0f, "Legendary Fish");

        private final float speedMultiplier;
        private final float erraticness;
        private final String name;

        FishDifficulty(float speedMultiplier, float erraticness, String name) {
            this.speedMultiplier = speedMultiplier;
            this.erraticness = erraticness;
            this.name = name;
        }

        public float getSpeedMultiplier() {
            return speedMultiplier;
        }

        public float getErraticness() {
            return erraticness;
        }

        public String getName() {
            return name;
        }
    }

    // Define fishing results
    public enum FishingResult {
        SUCCESS("Caught!", Color.GREEN),
        FAILED("Fish got away...", Color.RED),
        TIMEOUT("Time's up!", Color.ORANGE);

        private final String message;
        private final Color color;

        FishingResult(String message, Color color) {
            this.message = message;
            this.color = color;
        }

        public String getMessage() {
            return message;
        }

        public Color getColor() {
            return color;
        }
    }

    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    // Events
    private CatchMinigameListener listener;

    public interface CatchMinigameListener {
        void onFishingCaught(FishingResult result, FishDifficulty difficulty);
    }

    public CatchMinigame(float barX, float barY) {
        this.barX = barX;
        this.barY = barY;
        this.isActive = false;
        this.progress = 0f;
        this.gameTimer = 0f;
        this.result = FishingResult.FAILED;
        this.resultMessageTimer = 0f;
        this.difficulty = FishDifficulty.EASY;

        // Initialize positions
        this.greenBarY = barY + BAR_HEIGHT / 2 - GREEN_BAR_HEIGHT / 2;
        this.greenBarVelocity = 0f;
        this.isPressed = false;

        this.fishY = barY + BAR_HEIGHT / 2 - FISH_SIZE / 2;
        this.fishTargetY = fishY;
        this.fishDirection = 1f;
        this.fishSpeed = FISH_SPEED_BASE;
        this.fishChangeTimer = 0f;

        this.shapeRenderer = new ShapeRendererManager();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.5f);
        this.glyphLayout = new GlyphLayout();
    }

    public void setListener(CatchMinigameListener listener) {
        this.listener = listener;
    }

    public void setDifficulty(FishDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public FishingResult getResult() {
        return result;
    }

    public float getProgress() {
        return progress;
    }

    public void start() {
        isActive = true;
        gameTimer = 0f;
        progress = 0f;
        result = FishingResult.FAILED;
        resultMessageTimer = 0f;

        // Reset positions
        greenBarY = barY + BAR_HEIGHT / 2 - GREEN_BAR_HEIGHT / 2;
        greenBarVelocity = 0f;
        isPressed = false;

        // Randomize initial fish position
        fishY = barY + MathUtils.random(FISH_SIZE, BAR_HEIGHT - FISH_SIZE);
        fishTargetY = fishY;
        fishDirection = MathUtils.randomSign();
        fishSpeed = FISH_SPEED_BASE * difficulty.getSpeedMultiplier();
        fishChangeTimer = 0f;

        Gdx.app.log("CatchMinigame", "Started fishing minigame with difficulty: " + difficulty.getName());
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isShowingResult() {
        return !isActive && resultMessageTimer > 0;
    }

    public void togglePressed() {
        this.isPressed = !this.isPressed;
    }

    public void update(float delta) {
        if (isActive) {
            gameTimer += delta;

            // Update green bar physics
            updateGreenBar(delta);

            // Update fish AI
            updateFish(delta);

            // Check if fish is in green bar zone
            boolean fishInZone = isFishInGreenBar();

            // Update progress
            if (fishInZone) {
                progress += PROGRESS_RATE_IN_ZONE * delta;
            } else {
                progress += PROGRESS_RATE_OUT_ZONE * delta;
            }

            // Clamp progress
            progress = MathUtils.clamp(progress, 0f, 1f);

            // Check win/lose conditions
            if (progress >= 1f) {
                // Success!
                result = FishingResult.SUCCESS;
                endGame();
            } else if (progress <= 0f || gameTimer >= GAME_DURATION) {
                // Failed or timeout
                result = (gameTimer >= GAME_DURATION) ? FishingResult.TIMEOUT : FishingResult.FAILED;
                endGame();
            }
        }

        // Update result message timer
        if (resultMessageTimer > 0) {
            resultMessageTimer -= delta;
        }
    }

    private void updateGreenBar(float delta) {
        // Apply forces
        if (isPressed) {
            greenBarVelocity += LIFT_FORCE * delta;
        } else {
            greenBarVelocity -= GRAVITY * delta;
        }

        // Apply drag
        greenBarVelocity *= DRAG;

        // Update position
        greenBarY += greenBarVelocity * delta;

        // Keep within bounds
        if (greenBarY < barY) {
            greenBarY = barY;
            greenBarVelocity = 0f;
        } else if (greenBarY + GREEN_BAR_HEIGHT > barY + BAR_HEIGHT) {
            greenBarY = barY + BAR_HEIGHT - GREEN_BAR_HEIGHT;
            greenBarVelocity = 0f;
        }
    }

    private void updateFish(float delta) {
        fishChangeTimer += delta;

        // Change fish direction/target periodically
        if (fishChangeTimer >= FISH_CHANGE_INTERVAL / difficulty.getErraticness()) {
            fishChangeTimer = 0f;

            // Pick a new target position
            fishTargetY = barY + MathUtils.random(FISH_SIZE, BAR_HEIGHT - FISH_SIZE);

            // Add some randomness to speed
            fishSpeed = (FISH_SPEED_BASE + MathUtils.random(-FISH_SPEED_VARIANCE, FISH_SPEED_VARIANCE))
                    * difficulty.getSpeedMultiplier();
        }

        // Move fish towards target
        if (Math.abs(fishY - fishTargetY) > 5f) {
            if (fishY < fishTargetY) {
                fishY += fishSpeed * delta;
            } else {
                fishY -= fishSpeed * delta;
            }
        }

        // Keep fish within bounds
        fishY = MathUtils.clamp(fishY, barY, barY + BAR_HEIGHT - FISH_SIZE);
    }

    private boolean isFishInGreenBar() {
        Rectangle fishRect = new Rectangle(barX + BORDER_WIDTH, fishY, BAR_WIDTH - 2 * BORDER_WIDTH, FISH_SIZE);
        Rectangle greenBarRect = new Rectangle(barX + BORDER_WIDTH, greenBarY, BAR_WIDTH - 2 * BORDER_WIDTH, GREEN_BAR_HEIGHT);
        return fishRect.overlaps(greenBarRect);
    }

    private void endGame() {
        isActive = false;
        resultMessageTimer = RESULT_MESSAGE_DURATION;

        // Log fishing result
        Gdx.app.log("CatchMinigame", "===============================================");
        Gdx.app.log("CatchMinigame", "FISHING RESULT");
        Gdx.app.log("CatchMinigame", "Result: " + result);
        Gdx.app.log("CatchMinigame", "Difficulty: " + difficulty.getName());
        Gdx.app.log("CatchMinigame", "Final Progress: " + (progress * 100f) + "%");
        Gdx.app.log("CatchMinigame", "Time Elapsed: " + gameTimer + "s");
        Gdx.app.log("CatchMinigame", "===============================================");

        // Notify listener
        if (listener != null) {
            listener.onFishingCaught(result, difficulty);
        }
    }

    public void draw(SpriteBatch batch) {
        // If not active and not showing result, don't draw anything
        if (!isActive && !isShowingResult()) {
            return;
        }

        // End SpriteBatch to use ShapeRenderer
        batch.end();

        // Only draw the minigame if active
        if (isActive) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Draw main bar background
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.getShapeRenderer().rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);

            // Draw bar border
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.getShapeRenderer().rect(barX, barY, BAR_WIDTH, BORDER_WIDTH); // bottom
            shapeRenderer.getShapeRenderer().rect(barX, barY + BAR_HEIGHT - BORDER_WIDTH, BAR_WIDTH, BORDER_WIDTH); // top
            shapeRenderer.getShapeRenderer().rect(barX, barY, BORDER_WIDTH, BAR_HEIGHT); // left
            shapeRenderer.getShapeRenderer().rect(barX + BAR_WIDTH - BORDER_WIDTH, barY, BORDER_WIDTH, BAR_HEIGHT); // right

            // Draw green bar (player controlled)
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.getShapeRenderer().rect(barX + BORDER_WIDTH, greenBarY,
                    BAR_WIDTH - 2 * BORDER_WIDTH, GREEN_BAR_HEIGHT);

            // Draw fish
            boolean fishInZone = isFishInGreenBar();
            shapeRenderer.setColor(fishInZone ? Color.YELLOW : Color.RED);
            shapeRenderer.getShapeRenderer().rect(barX + BORDER_WIDTH, fishY,
                    BAR_WIDTH - 2 * BORDER_WIDTH, FISH_SIZE);

            shapeRenderer.end();

            // Draw progress bar
            float progressBarWidth = 200f;
            float progressBarHeight = 20f;
            float progressBarX = barX + BAR_WIDTH + 20f;
            float progressBarY = barY + BAR_HEIGHT - 50f;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Progress bar background
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.getShapeRenderer().rect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);

            // Progress bar fill
            shapeRenderer.setColor(progress > 0.7f ? Color.GREEN : progress > 0.3f ? Color.YELLOW : Color.RED);
            shapeRenderer.getShapeRenderer().rect(progressBarX, progressBarY, progressBarWidth * progress, progressBarHeight);

            // Progress bar border
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.getShapeRenderer().rect(progressBarX, progressBarY, progressBarWidth, 2f); // bottom
            shapeRenderer.getShapeRenderer().rect(progressBarX, progressBarY + progressBarHeight - 2f, progressBarWidth, 2f); // top
            shapeRenderer.getShapeRenderer().rect(progressBarX, progressBarY, 2f, progressBarHeight); // left
            shapeRenderer.getShapeRenderer().rect(progressBarX + progressBarWidth - 2f, progressBarY, 2f, progressBarHeight); // right

            shapeRenderer.end();
        }

        // Resume SpriteBatch for text rendering
        batch.begin();

        // Draw result message
        if (resultMessageTimer > 0) {
            String message = result.getMessage();
            Color messageColor = result.getColor();

            font.setColor(messageColor);
            glyphLayout.setText(font, message);
            font.draw(batch, message, barX + BAR_WIDTH / 2 - glyphLayout.width / 2, barY + BAR_HEIGHT + 40);
        }

        // Draw instructions and info if active
        if (isActive) {
            font.setColor(Color.WHITE);

            // Instructions
            String instruction = "Hold to lift the green bar!";
            glyphLayout.setText(font, instruction);
            font.draw(batch, instruction, barX + BAR_WIDTH / 2 - glyphLayout.width / 2, barY - 20);

            // Fish type
            String fishType = difficulty.getName();
            glyphLayout.setText(font, fishType);
            font.draw(batch, fishType, barX + BAR_WIDTH + 20, barY + BAR_HEIGHT - 100);

            // Timer
            String timeText = "Time: " + String.format("%.1f", GAME_DURATION - gameTimer);
            glyphLayout.setText(font, timeText);
            font.draw(batch, timeText, barX + BAR_WIDTH + 20, barY + BAR_HEIGHT - 130);

            // Progress percentage
            String progressText = "Progress: " + String.format("%.0f", progress * 100) + "%";
            glyphLayout.setText(font, progressText);
            font.draw(batch, progressText, barX + BAR_WIDTH + 20, barY + BAR_HEIGHT - 80);
        }
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}