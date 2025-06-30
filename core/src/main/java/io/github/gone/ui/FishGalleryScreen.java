package io.github.gone.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.gone.entities.Gallery;
import io.github.gone.entities.FishRegistry;
import io.github.gone.fish.FishFactory;
import io.github.gone.progression.ProgressionManager;
import io.github.gone.utils.ShapeRendererManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import io.github.gone.fish.Fish;

import java.util.Map;

/**
 * Screen that displays the fish gallery.
 */
public class FishGalleryScreen {
    // UI Constants
    private static final float SCREEN_WIDTH = Gdx.graphics.getWidth();
    private static final float SCREEN_HEIGHT = Gdx.graphics.getHeight();
    private static final float PANEL_WIDTH = SCREEN_WIDTH * 0.8f;
    private static final float PANEL_HEIGHT = SCREEN_HEIGHT * 0.7f;
    private static final float PANEL_X = (SCREEN_WIDTH - PANEL_WIDTH) / 2;
    private static final float PANEL_Y = (SCREEN_HEIGHT - PANEL_HEIGHT) / 2;
    
    // Button constants
    private static final float BUTTON_WIDTH = 200f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float BUTTON_PADDING = 15f;
    private static final float RESET_BUTTON_Y = PANEL_Y + 80f;
    private static final float CLOSE_BUTTON_Y = PANEL_Y + 20f;
    
    // Colors
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 0.7f);
    private static final Color PANEL_COLOR = new Color(0.1f, 0.1f, 0.1f, 0.9f);
    private static final Color TITLE_COLOR = new Color(0.2f, 0.4f, 0.8f, 1f);
    private static final Color TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.6f, 0.8f, 1f);
    private static final Color RESET_BUTTON_COLOR = new Color(0.8f, 0.2f, 0.2f, 1f);
    
    // Content
    private boolean isActive;
    
    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont textFont;
    private final BitmapFont buttonFont;
    private final GlyphLayout layout;
    private final SpriteBatch batch;
    
    // Gallery UI elements
    private final Stage stage;
    private final Skin skin;
    private final Table scrollTable;
    private final ScrollPane scrollPane;
    private Texture pixel;
    private Drawable knobDrawable;
    private Drawable scrollBackgroundDrawable;
    private TextButton resetButton;
    private TextButton closeButton;

    // Progression and Gallery managers
    private final ProgressionManager progressionManager;
    private final Gallery gallery;
    private final FishFactory fishFactory;
    
    // Callback for when reset is pressed
    public interface Callback {
        void onClose();
        void onReset();
    }
    
    private Callback callback;
    
    public FishGalleryScreen() {
        this.shapeRenderer = new ShapeRendererManager();
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2f);
        this.textFont = new BitmapFont();
        this.textFont.getData().setScale(1.2f);
        this.buttonFont = new BitmapFont();
        this.buttonFont.getData().setScale(1.2f);
        this.layout = new GlyphLayout();
        this.progressionManager = ProgressionManager.getInstance();
        this.gallery = Gallery.getInstance();
        this.fishFactory = new FishFactory();
        this.isActive = false;
        this.batch = new SpriteBatch();

        // Setup Scene2D for scrollable list
        stage = new Stage();
        skin = new Skin();
        // Generate a simple white 1x1 texture for the default Skin
        skin.add("default-font", textFont, BitmapFont.class);
        LabelStyle labelStyle = new LabelStyle(textFont, TEXT_COLOR);
        skin.add("default", labelStyle);

        // Create a 1x1 white pixel texture for drawables
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        // Create a 1x1 pixel texture for the scroll pane background with PANEL_COLOR
        Pixmap backgroundPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        backgroundPixmap.setColor(PANEL_COLOR);
        backgroundPixmap.fill();
        Texture backgroundPixel = new Texture(backgroundPixmap);
        backgroundPixmap.dispose();

        // Create drawables for ScrollPaneStyle
        TextureRegion pixelRegion = new TextureRegion(pixel);
        knobDrawable = new TextureRegionDrawable(pixelRegion);
        scrollBackgroundDrawable = new TextureRegionDrawable(backgroundPixel);

        // Create ScrollPaneStyle
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = scrollBackgroundDrawable;
        scrollStyle.hScroll = scrollBackgroundDrawable; // Horizontal scroll background
        scrollStyle.hScrollKnob = knobDrawable; // Horizontal scroll knob
        scrollStyle.vScroll = scrollBackgroundDrawable; // Vertical scroll background
        scrollStyle.vScrollKnob = knobDrawable; // Vertical scroll knob

        skin.add("default", scrollStyle);

        scrollTable = new Table(skin);
        scrollPane = new ScrollPane(scrollTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Only vertical scrolling
        scrollPane.setBounds(PANEL_X + 20, PANEL_Y + 150, PANEL_WIDTH - 40, PANEL_HEIGHT - 250);
        stage.addActor(scrollPane);

        // Create TextButton styles
        TextButtonStyle resetButtonSyle = new TextButtonStyle();
        resetButtonSyle.font = buttonFont;
        resetButtonSyle.fontColor = Color.WHITE;
        // Create a drawable for the reset button background
        Pixmap resetButtonPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        resetButtonPixmap.setColor(RESET_BUTTON_COLOR);
        resetButtonPixmap.fill();
        Texture resetButtonTexture = new Texture(resetButtonPixmap);
        resetButtonPixmap.dispose();
        resetButtonSyle.up = new TextureRegionDrawable(new TextureRegion(resetButtonTexture));
        skin.add("reset-button", resetButtonSyle);

        TextButtonStyle closeButtonStyle = new TextButtonStyle();
        closeButtonStyle.font = buttonFont;
        closeButtonStyle.fontColor = Color.WHITE;
        // Create a drawable for the close button background
        Pixmap closeButtonPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        closeButtonPixmap.setColor(BUTTON_COLOR);
        closeButtonPixmap.fill();
        Texture closeButtonTexture = new Texture(closeButtonPixmap);
        closeButtonPixmap.dispose();
        closeButtonStyle.up = new TextureRegionDrawable(new TextureRegion(closeButtonTexture));
        skin.add("close-button", closeButtonStyle);

        // Create and position the buttons
        resetButton = new TextButton("Reset Progress", skin, "reset-button");
        resetButton.setBounds(PANEL_X + (PANEL_WIDTH - BUTTON_WIDTH) / 2, RESET_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        stage.addActor(resetButton);

        closeButton = new TextButton("Close", skin, "close-button");
        closeButton.setBounds(PANEL_X + (PANEL_WIDTH - BUTTON_WIDTH) / 2, CLOSE_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        stage.addActor(closeButton);

        // Add listeners to buttons
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("FishGalleryScreen", "Player requested progress reset");
                if (callback != null) {
                    callback.onReset();
                }
            }
        });

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
                if (callback != null) {
                    callback.onClose();
                }
            }
        });
    }
    
    /**
     * Shows the fish gallery screen
     */
    public void show(Callback callback) {
        this.callback = callback;
        this.isActive = true;
        populateGallery();
    }
    
    /**
     * Hide the fish gallery screen
     */
    public void hide() {
        this.isActive = false;
    }

    /**
     * Populates the scrollable table with fish data.
     */
    private void populateGallery() {
        scrollTable.clearChildren(); // Clear existing entries
        
        Map<String, FishRegistry> caughtFish = gallery.getGallery();
        Map<String, Integer> allFishNames = fishFactory.getAllFishNames(); // Assuming FishFactory can provide all fish names

        // Add table headers
        scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("Fish Name", skin)).expandX().left().padBottom(5);
        scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("Caught", skin)).right().padBottom(5);
        scrollTable.row();
        scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("----------", skin)).expandX().left().padBottom(10);
        scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("------", skin)).right().padBottom(10);
        scrollTable.row();

        // Populate with actual data
        for (Map.Entry<String, Integer> entry : allFishNames.entrySet()) {
            String fishName = entry.getKey();
            if (caughtFish.containsKey(fishName)) {
                int count = caughtFish.get(fishName).nCaught;
                scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label(fishName, skin)).expandX().left().padBottom(5);
                scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label(String.valueOf(count), skin)).right().padBottom(5);
            } else {
                scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("???", skin)).expandX().left().padBottom(5);
                scrollTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("0", skin)).right().padBottom(5);
            }
            scrollTable.row();
        }
    }

    /**
     * Draw the fish gallery screen
     */
    public void draw() {
        if (!isActive) return;
        
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(OVERLAY_COLOR);
        shapeRenderer.getShapeRenderer().rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        // Draw panel background
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.getShapeRenderer().rect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw decorative header banner
        shapeRenderer.setColor(TITLE_COLOR);
        shapeRenderer.getShapeRenderer().rect(PANEL_X, PANEL_Y + PANEL_HEIGHT - 60, PANEL_WIDTH, 60);
        
        shapeRenderer.end();
        
        // Use this screen's own batch for the title text
        this.batch.begin();
        // Draw title
        titleFont.setColor(Color.WHITE);
        String title = "Fish Gallery";
        layout.setText(titleFont, title);
        titleFont.draw(this.batch, title, PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + PANEL_HEIGHT - 20);
        this.batch.end();
        
        // Draw the Scene2D stage (which contains the scroll pane and buttons)
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }
    
    /**
     * Handle touch input directly by the stage
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!isActive) return false;
        return stage.touchDown(screenX, screenY, pointer, button);
    }

    /**
     * Handle touch up directly by the stage
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!isActive) return false;
        return stage.touchUp(screenX, screenY, pointer, button);
    }

    /**
     * Handle touch dragged directly by the stage
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!isActive) return false;
        return stage.touchDragged(screenX, screenY, pointer);
    }

    /**
     * Handle mouse moved directly by the stage
     */
    public boolean mouseMoved(int screenX, int screenY) {
        if (!isActive) return false;
        return stage.mouseMoved(screenX, screenY);
    }

    /**
     * Handle scrolled directly by the stage
     */
    public boolean scrolled(float amountX, float amountY) {
        if (!isActive) return false;
        return stage.scrolled(amountX, amountY);
    }

    /**
     * Handle key down directly by the stage
     */
    public boolean keyDown(int keycode) {
        if (!isActive) return false;
        return stage.keyDown(keycode);
    }

    /**
     * Handle key up directly by the stage
     */
    public boolean keyUp(int keycode) {
        if (!isActive) return false;
        return stage.keyUp(keycode);
    }

    /**
     * Handle key typed directly by the stage
     */
    public boolean keyTyped(char character) {
        if (!isActive) return false;
        return stage.keyTyped(character);
    }

    /**
     * Check if the screen is currently active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (textFont != null) {
            textFont.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (pixel != null) {
            pixel.dispose();
        }
        if (((TextureRegionDrawable) scrollBackgroundDrawable).getRegion().getTexture() != null) {
            ((TextureRegionDrawable) scrollBackgroundDrawable).getRegion().getTexture().dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }

    public void toggleVisibility() {
        this.isActive = !this.isActive;
        if (this.isActive) {
            populateGallery(); // Repopulate gallery when shown
        }
        if (!this.isActive && callback != null) {
            callback.onClose(); // Call onClose if the gallery is being hidden
        }
    }

    public void addFish(Fish fish) {
        // This method will be called by GameManager to add a caught fish to the gallery
        // TODO: Implement storing and displaying collected fish
        System.out.println("FishGalleryScreen: Added fish to gallery: " + fish.getName());
    }
} 