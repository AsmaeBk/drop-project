package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    // Texture is the way that we store our images in the video ram.
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite; // Declare a new Sprite variable
    Array<Sprite> dropSprites;

    // We created a single instance of Vector instead of creating it locally
    // By reusing the Vector instance we prevent the game form triggering the garbage collector frequently
    // wich causes lags
    Vector2 touchPos;

    // dropTimer will keep track of how much time has elapsed between each spawn.
    float dropTimer;

    // We need two rectangles to make comparisons. One for the bucket and one to be reused with every drop.
    // because the drops don’t interact with the bucket.
    // This is where we incorporate some rudimentary collision detection. This can be achieved with the Rectangle class.
    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    public Main(Texture backgroundTexture, Texture bucketTexture, Texture dropTexture, Sound dropSound, Music music, SpriteBatch spriteBatch, FitViewport viewport, Sprite bucketSprite, Vector2 touchPos) {
        this.backgroundTexture = backgroundTexture;
        this.bucketTexture = bucketTexture;
        this.dropTexture = dropTexture;
        this.dropSound = dropSound;
        this.music = music;
        this.spriteBatch = spriteBatch;
        this.viewport = viewport;
        this.bucketSprite = bucketSprite;
        this.touchPos = touchPos;
    }

    public Main() {

    }

    @Override
    public void create() {

        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal(("music.mp3")));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        bucketSprite = new Sprite(bucketTexture); // Initialize the sprite based on the texture
        bucketSprite.setSize(1, 1); // Define the size of the sprite
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        music.setLooping(true);
        music.setVolume(.5f);
       // music.play();

    }

    @Override
    public void render() {
        // organize code into three methods
        input();
        logic();
        draw();

    }

    private void input() {
        // Speed
        float speed = .25f;
        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            bucketSprite.translateX(speed*delta);
        } else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed*delta);

        }
        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened in the screen
            viewport.unproject(touchPos); // Convert the units to the world units of the viewport
            bucketSprite.setCenterX(touchPos.x);// Change the horizontally centered position of the bucket
        }
    }

    private void logic() {
        // Store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Store the bucket size for brevity
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        // Clamp x to values between 0 and worldWidth
        // clamp : if (value < min) return min; if (value > max) return max;
        // bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth));
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        // Loop through the sprites backwards to prevent out of bounds errors
        // Apply the bucket position and size to the bucketRectangle
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            // Apply the drop position and size to the dropRectangle
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) { // Check if the bucket overlaps the drop
                dropSprites.removeIndex(i); // Remove the drop
                dropSound.play(); // Play the sound
            }
        }


        dropTimer += delta; // Adds the current delta to the timer
        if (dropTimer > 1f) { // Check if it has been more than a second
            dropTimer = 0; // Reset the timer
            createDroplet(); // Create the droplet
        }
    }

    private void draw() {
        // clears the screen. It’s a good practice to clear the screen every frame. Otherwise, you’ll get weird graphical errors.
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        // store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.begin();


        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // draw the background
        bucketSprite.draw(spriteBatch); // Sprites have their own draw method

        // draw each sprite
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
        // create local variables for convenience
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // create the drop sprite
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);

        //we’re subtracting the width of the sprite so none of the raindrops appear outside of the view.
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth)); // Randomize the drop's x position

        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite); // Add it to the list
    }
    @Override
    public void dispose() {

    }
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


}
