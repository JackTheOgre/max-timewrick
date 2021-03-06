
package com.luna.timewrick;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;

public class MapRenderer {
	final float CAMERA_SPEED = 10f;
    final float BACKGROUND_Y = 10;
    Map map;
	OrthographicCamera cam;
	SpriteCache cache;
	SpriteBatch batch = new SpriteBatch(5460);
	ImmediateModeRenderer20 renderer = new ImmediateModeRenderer20(false, true, 0);
	int[][] blocks;
	TextureRegion tile;
	TextureRegion grass;
	TextureRegion wood;
	TextureRegion door;
	TextureRegion gold;
	Animation maxLeft;
	Animation maxRight;
	Animation maxJumpLeft;
	Animation maxJumpRight;
	Animation maxIdleLeft;
	Animation maxIdleRight;
	Animation maxDead;
	Animation zap;
	TextureRegion dispenser;
	Animation spawn;
	Animation dying;
	Animation rocketExplosion;
	TextureRegion endDoor;

    TextureRegion[] roomBackground = new TextureRegion[Map.AMOUNT_OF_ROOMS];
	FPSLogger fps = new FPSLogger();

	public MapRenderer (Map map) {
		this.map = map;
		this.cam = new OrthographicCamera(24, 16);
		this.cam.position.set(map.max.pos.x, map.max.pos.y, 0);
		this.cam.zoom = 0.7f;
		this.cache = new SpriteCache(this.map.tiles.length * this.map.tiles[0].length, false);
		this.blocks = new int[(int)Math.ceil(this.map.tiles.length / 24.0f)][(int)Math.ceil(this.map.tiles[0].length / 16.0f)];

		createAnimations();
		createBlocks();
	}
    static float minBlockY, maxBlockY;
	private void createBlocks () {
		int width = map.tiles.length;
		int height = map.tiles[0].length;
		for (int blockY = 0; blockY < blocks[0].length; blockY++) {
			for (int blockX = 0; blockX < blocks.length; blockX++) {
				cache.beginCache();
				for (int y = blockY * 16; y < blockY * 16 + 16; y++) {
					for (int x = blockX * 24; x < blockX * 24 + 24; x++) {
						if (x > width) continue;
						if (y > height) continue;
						int posX = x;
						int posY = height - y - 1;
						if(x<width&&y<height) {
							if (map.match(map.tiles[x][y], Map.DIRT)) cache.add(tile, posX, posY, 1, 1);
							else if (map.match(map.tiles[x][y], Map.GRASS)) cache.add(grass, posX, posY, 1, 1);
							else if (map.match(map.tiles[x][y], Map.WOOD)) cache.add(wood, posX, posY, 1, 1); //при добавлении материала не забудь здесь
							else if (map.match(map.tiles[x][y], Map.GOLD)) cache.add(gold, posX, posY, 1, 1);
						}
					}
				}
				blocks[blockX][blockY] = cache.endCache();
                for (int i = 0; i < blocks.length; i++) {
                    for (int j = 0; j < blocks[0].length; j++) {
                        if(blocks[i][j]<minBlockY) minBlockY = blocks[i][j];
                        if(blocks[i][j]>maxBlockY) maxBlockY = blocks[i][j];
                    }
                }
            }
		}
		Gdx.app.debug("Timewrick", "blocks created");
	}

	private void createAnimations () {
        for (int i = 0; i < Map.AMOUNT_OF_ROOMS; i++) {
            roomBackground[i] = new TextureRegion(new Texture(Gdx.files.internal("data/rooms/"+i+".png"))); // TODO: 23.11.15 mb width=roomEnd[i]-room[start]-1
        }

		this.tile = new TextureRegion(new Texture(Gdx.files.internal("data/tile.png")), 0, 0, 20, 20);
		Texture maxTexture = new Texture(Gdx.files.internal("data/max2.png"));
		TextureRegion[] splitMax = new TextureRegion(maxTexture).split(20, 20)[0];
		TextureRegion[] mirrorMax = new TextureRegion(maxTexture).split(20, 20)[0];
		Texture mainTexture = new Texture(Gdx.files.internal("data/texture.png"));
		TextureRegion[] splitMain = new TextureRegion(maxTexture).split(20, 20)[0];
		for (TextureRegion region : mirrorMax)
			region.flip(true, false);
		maxRight = new Animation(0.1f, splitMax[0], splitMax[1]);
		maxLeft = new Animation(0.1f, mirrorMax[0], mirrorMax[1]);
		maxJumpRight = new Animation(0.1f, splitMax[2], splitMax[3]);
		maxJumpLeft = new Animation(0.1f, mirrorMax[2], mirrorMax[3]);
		maxIdleRight = new Animation(0.5f, splitMax[0], splitMax[4]);
		maxIdleLeft = new Animation(0.5f, mirrorMax[0], mirrorMax[4]);
		maxDead = new Animation(0.2f, splitMax[0]);

		splitMax = new TextureRegion(maxTexture).split(20, 20)[1];
		spawn = new Animation(0.1f, splitMain[4], splitMain[3], splitMain[2], splitMain[1]);
		dying = new Animation(0.1f, splitMain[1], splitMain[2], splitMain[3], splitMain[4]);
		dispenser = splitMain[5];

		splitMain = new TextureRegion(mainTexture).split(20, 20)[0];
		rocketExplosion = new Animation(0.1f, splitMain[0], splitMain[1], splitMain[2], splitMain[3], splitMain[4], splitMain[4]);

		splitMain = new TextureRegion(mainTexture).split(20, 20)[1];
		grass = new TextureRegion(splitMain[0]);
		wood = new TextureRegion(splitMain[1]);
		gold = new TextureRegion(splitMain[2]);

		splitMain = new TextureRegion(mainTexture).split(20, 20)[2];
		door = new TextureRegion(splitMain[0]);

		splitMain = new TextureRegion(mainTexture).split(20, 20)[3];
		endDoor = splitMain[2];
	}

	float stateTime = 0;
	Vector3 lerpTarget = new Vector3();

	public void render (float deltaTime) {
		float roomStart = map.roomStart[map.max.curRoom];
		float roomEnd = map.roomEnd[map.max.curRoom];
		if(map.max.vel.x>0) {
            cam.position.lerp(lerpTarget.set(Math.min(roomEnd, Math.max(roomStart,map.max.pos.x)), map.max.pos.y, 0), CAMERA_SPEED * deltaTime); // TODO: 23.11.15 пофиксить верхнюю и нижнюю границу камеру
        } else {
            cam.position.lerp(lerpTarget.set(Math.max(roomStart, Math.min(roomEnd, map.max.pos.x)), map.max.pos.y, 0), CAMERA_SPEED * deltaTime);
        }

//		cam.position.lerp(lerpTarget.set(21f, map.max.pos.y, 0), 2f * deltaTime);
		cam.update();
        cache.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        batch.draw(roomBackground[map.max.curRoom-1], map.roomStart[map.max.curRoom-1] - Map.HALFSCREEN_X -1, 10, 2+2*Map.HALFSCREEN_X + (map.roomEnd[map.max.curRoom-1] - map.roomStart[map.max.curRoom-1]), 24);// TODO: 23.11.15 ONLY IF ROOM IS CHANGING
        batch.draw(roomBackground[map.max.curRoom+1], map.roomStart[map.max.curRoom+1] - Map.HALFSCREEN_X -1, 10, 2+2*Map.HALFSCREEN_X +(map.roomEnd[map.max.curRoom+1] - map.roomStart[map.max.curRoom+1]), 24);
        batch.draw(roomBackground[map.max.curRoom], map.roomStart[map.max.curRoom] - Map.HALFSCREEN_X -1, 10, 2+2*Map.HALFSCREEN_X + (map.roomEnd[map.max.curRoom] - map.roomStart[map.max.curRoom]), 24);
        batch.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		cache.begin();
		int b = 0;
		for (int blockY = 0; blockY < Math.min(4, blocks[0].length); blockY++) {
			for (int blockX = 0; blockX < Math.min(6, blocks.length); blockX++) {
				cache.draw(blocks[blockX][blockY]);
				b++;
			}
		}
		cache.end();
		stateTime += deltaTime;
        batch.begin();
        for(int i = 0; i< map.doors.size(); i++) {
			Door d = map.doors.get(i);
			if(i%2==0) {
				batch.draw(door, d.bounds.x, d.bounds.y, 1, 1);
			} else {
				door.flip(true, false);
				batch.draw(door, d.bounds.x, d.bounds.y, 1, 1);
				door.flip(true, false);
			}
		}
		if (map.endDoor != null) batch.draw(endDoor, map.endDoor.bounds.x, map.endDoor.bounds.y, 1, 1);
		renderMax();
		batch.end();

		fps.log();
	}

	private void renderMax () {
		Animation anim = null;
		boolean loop = true;
		if (map.max.state == Max.RUN) {
			if (map.max.dir == Max.LEFT)
				anim = maxLeft;
			else
				anim = maxRight;
		}
		if (map.max.state == Max.IDLE) {
			if (map.max.dir == Max.LEFT)
				anim = maxIdleLeft;
			else
				anim = maxIdleRight;
		}
		if (map.max.state == Max.JUMP) {
			if (map.max.dir == Max.LEFT)
				anim = maxJumpLeft;
			else
				anim = maxJumpRight;
		}
		if (map.max.state == Max.SPAWN) {
			anim = spawn;
			loop = false;
		}
		if (map.max.state == Max.DYING) {
			anim = dying;
			loop = false;
		}
		batch.draw(anim.getKeyFrame(map.max.stateTime, loop), map.max.pos.x, map.max.pos.y, 1, 1);
	}


	public void dispose () {
		cache.dispose();
		batch.dispose();
		tile.getTexture().dispose();
	}
}
