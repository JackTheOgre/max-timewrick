package com.luna.timewrick;

import com.badlogic.gdx.math.Rectangle;

/**
 * Created by luna on 19.11.15.
 */
public class Door {
    public Rectangle bounds = new Rectangle();

    public Door (float x, float y) {
        this.bounds.x = x;
        this.bounds.y = y;
        this.bounds.width = this.bounds.height = 1;
    }
}
