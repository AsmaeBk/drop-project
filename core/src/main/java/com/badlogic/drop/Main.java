package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import javax.crypto.Mac;

public class Main extends ApplicationAdapter {   // Better to extend ApplicationAdapter

    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    FitViewport uiViewport; //for UI (Pixels)

    Sprite bucketSprite;

    Vector2 touchPos;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    float dropTimer = 0;

    int score;
    BitmapFont font;

    boolean gameOver;

    Texture goldTexture;
    Texture blueTexture;
    Texture redTexture;

    // Change from Array<Sprite> to your new polymorphic base class!
    Array<Drop> dropSprites;
    @Override
    public void create() {
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        blueTexture= new Texture("drop.png");
        goldTexture= new Texture("goldDrop.png");
        redTexture= new Texture("redDrop.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        // Initialize the UI viewport with pixel units
        uiViewport = new FitViewport(800, 600);

        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        bucketSprite.setPosition(3.5f, 0.5f);   // Better starting position (center)

        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        music.setLooping(true);
        music.setVolume(0.5f);
        // music.play();

        score=3;
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        // ADD THIS LINE TO SMOOTH OUT THE PIXELS:
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        gameOver = false;

    }

    @Override
    public void render() {
        if(!gameOver && score < 10) {
            input();
            logic();
            draw();
        } else {
            isSpaceKeyJustpressed();
            draw();
        }

    }
    private void isSpaceKeyJustpressed() {

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            score = 3;                  // Reset lives/score
            dropSprites.clear();        // Clear all active raindrops off the screen
            dropTimer = 0;              // Reset the spawn timer

            // Reposition the bucket back to the center
            bucketSprite.setPosition(3.5f, 0.5f);

            gameOver = false;           // Turn off the game over flag to resume gameplay
        }
    }
    private void input() {
        float speed = 5f;                    // Much better speed
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }


    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float bucketWidth = bucketSprite.getWidth();

        // Keep bucket inside screen
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketSprite.getHeight());

        // Update drops
        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Drop drop = dropSprites.get(i);
            drop.fall(delta);      // Faster falling

            dropRectangle.set(drop.getX(), drop.getY(), drop.getWidth(), drop.getHeight());

            if (drop.getY() < -drop.getHeight()) {
                dropSprites.removeIndex(i);

                // Penalty for missing a drop: only subtract if it wasn't an enemy Red drop!
                if (!(drop instanceof RedDrop)) {
                    score--;
                }
            } else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
                score += drop.getScoreValue();
            }

            if(score==0) {
                gameOver=true;
            }
        }

        // Spawn new drops
        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
        // DRAW GAME WORLD (8x5 units) ----
        if (gameOver ) {
            // Light Blue: Red=0.6, Green=0.8, Blue=1.0, Alpha=1.0 (fully visible)
            ScreenUtils.clear(Color.ORANGE);
        } else if(score== 10) {
            ScreenUtils.clear(0.6f, 0.8f, 1.0f, 1.0f);

        }
            else {
            // Default Black screen for playing or winning
            ScreenUtils.clear(Color.BLACK);
        }
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.begin();


        if(!gameOver&& score < 10) {
            spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
            bucketSprite.draw(spriteBatch);
            for (Sprite drop : dropSprites) {
                drop.draw(spriteBatch);
            }
        }
        spriteBatch.end();
        //DRAW UI (800x600 pixels) ----
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        spriteBatch.begin();
        // Position the text using the 800x600 UI space
        // 20 pixels from the left, 30 pixels from the top edge (600 - 30)
        font.draw(spriteBatch, "Score: " + score, 20, 570);

        if(gameOver) {
            font.draw(spriteBatch, "Press space to restart", 350, 350);
        }
        if(score == 10) {
            font.draw(spriteBatch, "YOU WIN!", 350, 350);
            font.draw(spriteBatch, "Press space to play again", 260, 300);
        }
        spriteBatch.end();
    }

    private void createDroplet() {
       double rand = MathUtils.random();
       Drop newDrop ;
       // 15% chance for Gold, 25% chance for Red, 60% chance for Blue
        if (rand < 0.15f) {
            newDrop = new GoldDrop(goldTexture);
        } else if (rand < 0.40f) {
            newDrop = new RedDrop(redTexture);
        } else {
            newDrop = new BlueDrop(blueTexture);
        }

        float worldWidth = viewport.getWorldWidth();
        newDrop.setX(MathUtils.random(0f, worldWidth - 1f));
        newDrop.setY(viewport.getWorldHeight());
        dropSprites.add(newDrop);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        bucketTexture.dispose();
        dropTexture.dispose();
        dropSound.dispose();
        music.dispose();
        spriteBatch.dispose();
        font.dispose();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true); // Add this line
    }
}
