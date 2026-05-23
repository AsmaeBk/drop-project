package com.badlogic.drop;

import com.badlogic.gdx.graphics.Texture;

class GoldDrop extends Drop {
    public GoldDrop(Texture texture) {
        super(texture, 4f); // Gold drops fall slightly faster!
    }

    @Override
    public int getScoreValue() {
        return 5;
    }
}
