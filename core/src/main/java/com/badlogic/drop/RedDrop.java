package com.badlogic.drop;

import com.badlogic.gdx.graphics.Texture;

public class RedDrop extends Drop {

    public RedDrop(Texture texture) {
        super(texture, 3.5f); // Tricky medium speed
    }

    @Override
    public int getScoreValue() {
        return -1;
    }
}
