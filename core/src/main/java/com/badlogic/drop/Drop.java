package com.badlogic.drop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

abstract class Drop extends Sprite {
    private float speed ;

    public Drop(Texture texture, float speed) {
        super(texture);
        this.speed = speed;
        this.setSize(1,1);
    }

    public void fall(float delta) {
        this.translateY(-speed * delta);
    }

    public abstract int getScoreValue();
}
