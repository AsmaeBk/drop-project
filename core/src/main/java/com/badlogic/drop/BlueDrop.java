package com.badlogic.drop;

import com.badlogic.gdx.graphics.Texture;

public class BlueDrop extends Drop {

    public BlueDrop(Texture texture) {
        super(texture, 3f);
    }

    @Override
    public int getScoreValue() {
        return 1;
    }
}
