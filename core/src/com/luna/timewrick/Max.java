package com.luna.timewrick;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by luna on 10.11.15.
 */
public class Max {
    static final int IDLE = 0;
    static final int RUN = 1;
    static final int JUMP = 2;
    static final int SPAWN = 3;
    static final int DYING = 4;
    static final int DEAD = 5;
    static final int LEFT = -1;
    static final int RIGHT = 1;

    static final float DOORTIME = 0;//время, необходимое для получения возможности снова пройти через дверь

    static final float ACCELERATION = 20f;
    static final float JUMP_VELOCITY = 10.4f;
    static final float GRAVITY = 25.0f;
    static final float MAX_VEL = 6f;
    static final float DAMP = 0.90f;

    Vector2 pos = new Vector2();
    Vector2 accel = new Vector2(); //NOT GONNA USE X
    Vector2 vel = new Vector2();
    public Rectangle bounds = new Rectangle();

    float doorTime = DOORTIME;
    int state = SPAWN;
    public int curRoom = 1;
    float stateTime = 0;
    int dir = LEFT;
    Map map;
    boolean grounded = false;

    public Max(Map map, float x, float y) {
        this.map = map;
        pos.x = x;
        pos.y = y;
        state = SPAWN;
        stateTime = 0;
        bounds.width = 0.6f;//TODO:WIDTH&HEIGHT&other
        bounds.height = 0.8f;
        bounds.x = pos.x-0.2f; //why?
        bounds.y = pos.y;
    }

    public void update(float deltaTime) {
        processKeys();

        accel.y = -GRAVITY;
        accel.scl(deltaTime);
        vel.add(0, accel.y); //TODO:IF ADD ACCEL.X THEN CHANGE
        vel.scl(deltaTime);
        pos.add(vel.x, vel.y);
//        Gdx.app.debug("Timewrick", "velocity is "+vel.x);
        tryMove();
        vel.scl(1.0f/deltaTime);


        if(state == SPAWN) {
            if(stateTime>0.4f) {
                state = IDLE;
            }
        }

        if(state == DYING) {
            if(stateTime>0.4f) {
                state = DEAD;
            }
        }

        stateTime+=deltaTime;
        doorTime+=deltaTime;
    }

    private void processKeys() {
        float x0 = (Gdx.input.getX(0) / (float)Gdx.graphics.getWidth()) * 480;
        float x1 = (Gdx.input.getX(1) / (float)Gdx.graphics.getWidth()) * 480;
        float y0 = 320 - (Gdx.input.getY(0) / (float)Gdx.graphics.getHeight()) * 320;

        boolean leftButton = (Gdx.input.isTouched(0) && x0 < 70) || (Gdx.input.isTouched(1) && x1 < 70);
        boolean rightButton = (Gdx.input.isTouched(0) && x0 > 70 && x0 < 134) || (Gdx.input.isTouched(1) && x1 > 70 && x1 < 134);
        boolean jumpButton = (Gdx.input.isTouched(0) && x0 > 416 && x0 < 480 && y0 < 64)|| (Gdx.input.isTouched(1) && x1 > 416 && x1 < 480 && y0 < 64);

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || jumpButton) && state != JUMP) {
            state = JUMP;
            vel.y = JUMP_VELOCITY;
            grounded = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A) || leftButton) {
            if (state != JUMP) state = RUN;
            dir = LEFT;
            vel.x = MAX_VEL*dir;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || rightButton) {
            if (state != JUMP) state = RUN;
            dir = RIGHT;
            vel.x = MAX_VEL*dir;
        } else {
            if (state != JUMP) state = IDLE;
            vel.x = 0;
        }


    }

    Rectangle[] r = {new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()};

    private void tryMove() {
        bounds.x += vel.x;
        fetchCollidableRects();
        for (int i = 0; i < r.length; i++) {
            Rectangle rect = r[i];
            if(bounds.overlaps(rect)) {
                if (vel.x < 0)
                    bounds.x = rect.x + rect.width + 0.01f;
                else
                    bounds.x = rect.x - bounds.width - 0.01f;                      // TODO: 10.11.15 UNDERSTAND!
                vel.x = 0;
            }
        }

        bounds.y += vel.y;
        fetchCollidableRects();
        for (int i = 0; i < r.length; i++) {
            Rectangle rect = r[i];
            if (bounds.overlaps(rect)) {
                if (vel.y < 0) {
                    bounds.y = rect.y + rect.height + 0.01f;
                    grounded = true;
                    if (state != DYING && state != SPAWN) state = Math.abs(accel.x) > 0.1f ? RUN : IDLE;
                } else
                    bounds.y = rect.y - bounds.height - 0.01f;
                vel.y = 0;
            }
        }

        pos.x = bounds.x - 0.2f; // TODO: 10.11.15 UNDERSTAND WHY -0.2F
        pos.y = bounds.y; 

    }

    private void fetchCollidableRects () {
        int p1x = (int)bounds.x;// TODO: 10.11.15 make rect array's length 3
        int p1y = (int)Math.floor(bounds.y);
        int p2x = (int)(bounds.x + bounds.width);
        int p2y = (int)Math.floor(bounds.y);
        int p3x = (int)(bounds.x + bounds.width);
        int p3y = (int)(bounds.y + bounds.height);
        int p4x = (int)bounds.x;
        int p4y = (int)(bounds.y + bounds.height);

        int[][] tiles = map.tiles;
        int tile1 = tiles[p1x][map.tiles[0].length - 1 - p1y];
        int tile2 = tiles[p2x][map.tiles[0].length - 1 - p2y];
        int tile3 = tiles[p3x][map.tiles[0].length - 1 - p3y];
        int tile4 = tiles[p4x][map.tiles[0].length - 1 - p4y];

        if (state != DYING && (map.isDeadly(tile1) || map.isDeadly(tile2) || map.isDeadly(tile3) || map.isDeadly(tile4))) {
            state = DYING;
            stateTime = 0;
        }
        for (int i = 0; i < map.doors.size(); i++) {
            Door d = map.doors.get(i);
            if(i%2==0) {
                if(this.pos.x>=d.bounds.x-0.2f&&this.pos.x<=d.bounds.x+0.2f && this.vel.x>0&&doorTime>DOORTIME) {
                    curRoom++;
                    this.bounds.x = map.doors.get(i+1).bounds.x+0.1f;//либо map.roomEnd[curRoom]+1 -Map.HALFSCREEN
//                    this.pos.x = map.roomStart[curRoom]+1;
                    doorTime = 0;
                }
            } else {
                if(this.pos.x>=d.bounds.x-0.2f&&this.pos.x<=d.bounds.x+0.2f && this.vel.x<0&&doorTime>DOORTIME) {
                    curRoom--;
                    this.bounds.x = map.doors.get(i-1).bounds.x+0.1f; //либо  map.roomEnd[curRoom]-1 +Map.HALFSCREEN

//                    this.pos.x = map.roomEnd[curRoom]-1;
                    doorTime = 0;
                }
            }
        }
        if (Map.isSolid(tile1))
            r[0].set(p1x, p1y, 1, 1);
        else
            r[0].set(-1, -1, 0, 0);
        if (Map.isSolid(tile2))
            r[1].set(p2x, p2y, 1, 1);
        else
            r[1].set(-1, -1, 0, 0);
        if (Map.isSolid(tile3))
            r[2].set(p3x, p3y, 1, 1);
        else
            r[2].set(-1, -1, 0, 0);
        if (Map.isSolid(tile4)) 
            r[3].set(p4x, p4y, 1, 1);
        else
            r[3].set(-1, -1, 0, 0);

    }

}
