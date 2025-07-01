package io.github.gone.minigames;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.gone.states.GameState; // Assuming GameState is in states package

public class MinigameManager {
    // Available minigames
    private ThrowMinigame throwMinigame;
    private CatchMinigame catchMinigame;

    // Active minigame tracking
    private MinigameType activeMinigameType;
    private boolean isPressed = false; // For fishing minigame input

    // Minigame types enum
    public enum MinigameType {
        NONE,
        THROW,
        CATCH
    }

    // Position for minigames
    private final float centerX;
    private final float centerY;

    public MinigameManager(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.activeMinigameType = MinigameType.NONE;

        // Initialize minigames
        this.throwMinigame = new ThrowMinigame(centerX, centerY);
        this.catchMinigame = new CatchMinigame(centerX - 30, centerY); // Offset for better positioning
    }

    public void setupMinigame(GameState state) {
        // This method will be responsible for setting up the correct minigame
        // based on the GameState or other game logic.
        // Implementation depends on how GameState determines which minigame to use
//        switch (state) {
//            // Example usage - adjust according to your GameState implementation
//            // case FISHING:
//            //     setActiveMinigame(MinigameType.FISHING);
//            //     break;
//            // case THROWING:
//            //     setActiveMinigame(MinigameType.THROW);
//            //     break;
//            default:
//                setActiveMinigame(MinigameType.NONE);
//                break;
//        }
    }

    /**
     * Sets the active minigame type
     */
    public void setActiveMinigame(MinigameType type) {
        // Stop current minigame if any is active
        stopCurrentMinigame();

        this.activeMinigameType = type;
    }

    /**
     * Gets the currently active minigame type
     */
    public MinigameType getActiveMinigameType() {
        return activeMinigameType;
    }

    /**
     * Stops the currently active minigame
     */
    private void stopCurrentMinigame() {
        // Note: Minigames don't have explicit stop methods, they handle their own lifecycle
        // If needed, you could add stop methods to the minigame classes
        activeMinigameType = MinigameType.NONE;
        isPressed = false;
    }

    /**
     * Starts the throw minigame
     */
    public void startThrowMinigame() {
        setActiveMinigame(MinigameType.THROW);
        if (this.throwMinigame == null) {
            this.throwMinigame = new ThrowMinigame(centerX, centerY);
        }
        this.throwMinigame.start();
    }

    /**
     * Starts the throw minigame at specific position
     */
    public void startThrowMinigame(float centerX, float centerY) {
        setActiveMinigame(MinigameType.THROW);
        if (this.throwMinigame == null) {
            this.throwMinigame = new ThrowMinigame(centerX, centerY);
        }
        this.throwMinigame.start();
    }

    /**
     * Starts the fishing minigame with specified difficulty
     */
    public void startCatchMinigame(CatchMinigame.FishDifficulty difficulty) {
        setActiveMinigame(MinigameType.CATCH);
        if (this.catchMinigame == null) {
            this.catchMinigame = new CatchMinigame(centerX - 30, centerY);
        }
        this.catchMinigame.setDifficulty(difficulty);
        this.catchMinigame.start();
    }

    /**
     * Starts the fishing minigame with default difficulty
     */
    public void startCatchMinigame() {
        startCatchMinigame(CatchMinigame.FishDifficulty.MEDIUM);
    }

    /**
     * Starts the fishing minigame at specific position
     */
    public void startCatchMinigame(float barX, float barY, CatchMinigame.FishDifficulty difficulty) {
        setActiveMinigame(MinigameType.CATCH);
        if (this.catchMinigame == null) {
            this.catchMinigame = new CatchMinigame(barX, barY);
        }
        this.catchMinigame.setDifficulty(difficulty);
        this.catchMinigame.start();
    }

    public void update(float delta) {
        switch (activeMinigameType) {
            case THROW:
                if (throwMinigame != null && (throwMinigame.isActive() || throwMinigame.isShowingResult())) {
                    throwMinigame.update(delta);
                }
                break;

            case CATCH:
                if (catchMinigame != null && (catchMinigame.isActive() || catchMinigame.isShowingResult())) {
                    catchMinigame.update(delta);
                }
                break;

            // No active minigame
            case NONE:
            default: break;
        }
    }

    public void draw(SpriteBatch batch) {
        switch (activeMinigameType) {
            case THROW:
                if (throwMinigame != null && (throwMinigame.isActive() || throwMinigame.isShowingResult())) {
                    throwMinigame.draw(batch);
                }
                break;

            case CATCH:
                if (catchMinigame != null && (catchMinigame.isActive() || catchMinigame.isShowingResult())) {
                    catchMinigame.draw(batch);
                }
                break;

            // No active minigame to draw
            case NONE:
            default: break;
        }
    }

    /**
     * Handles click input - delegates to appropriate minigame
     */
    public void onClick() {
        switch (activeMinigameType) {
            case THROW:
                if (throwMinigame != null && throwMinigame.isActive()) {
                    throwMinigame.onClick();
                }
                break;

            case CATCH:
                // Fishing uses press/release rather than click
                togglePress();
                break;

            // No active minigame
            case NONE:
            default: break;
        }
    }

    /**
     * Handles mouse/touch release - mainly for fishing minigame
     */
    public void onRelease() {
        switch (activeMinigameType) {
            case CATCH:
                togglePress();
                break;

            // Other minigames don't use release events
            case THROW:
            case NONE:
            default:
                break;
        }
    }

    /**
     * Sets pressed state for fishing minigame
     */
    public void togglePress() {
        this.isPressed = !this.isPressed;
        if (activeMinigameType == MinigameType.CATCH && catchMinigame != null) {
            catchMinigame.togglePressed();
        }
    }

    /**
     * Gets pressed state
     */
    public boolean isPressed() {
        return isPressed;
    }

    /**
     * Checks if any minigame is currently active or showing results
     */
    public boolean isMinigameActive() {
        return switch (activeMinigameType) {
            case THROW -> throwMinigame != null && (throwMinigame.isActive() || throwMinigame.isShowingResult());
            case CATCH -> catchMinigame != null && (catchMinigame.isActive() || catchMinigame.isShowingResult());
            default -> false;
        };
    }

    /**
     * Checks if a specific minigame is active
     */
    public boolean isMinigameActive(MinigameType type) {
        if (activeMinigameType != type) {
            return false;
        }

        return switch (type) {
            case THROW -> throwMinigame != null && throwMinigame.isActive();
            case CATCH -> catchMinigame != null && catchMinigame.isActive();
            default -> false;
        };
    }

    /**
     * Checks if a specific minigame is showing results
     */
    public boolean isMinigameShowingResult(MinigameType type) {
        if (activeMinigameType != type) {
            return false;
        }

        return switch (type) {
            case THROW -> throwMinigame != null && throwMinigame.isShowingResult();
            case CATCH -> catchMinigame != null && catchMinigame.isShowingResult();
            default -> false;
        };
    }

    // Getters for individual minigames
    public ThrowMinigame getThrowMinigame() {
        return throwMinigame;
    }

    public CatchMinigame getCatchMinigame() {
        return catchMinigame;
    }

    // Convenience methods to set listeners
    public void setThrowMinigameListener(ThrowMinigame.ThrowMinigameListener listener) {
        if (throwMinigame != null) {
            throwMinigame.setListener(listener);
        }
    }

    public void setFishingMinigameListener(CatchMinigame.CatchMinigameListener listener) {
        if (catchMinigame != null) {
            catchMinigame.setListener(listener);
        }
    }

    public void dispose() {
        if (throwMinigame != null) {
            throwMinigame.dispose();
        }
        if (catchMinigame != null) {
            catchMinigame.dispose();
        }
    }
}