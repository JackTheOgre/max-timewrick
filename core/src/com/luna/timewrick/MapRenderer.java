
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
	Animation maxLeft;
	Animation maxRight;
	Animation maxJumpLeft;
	Animation maxJumpRight;
	Animation maxIdleLeft;
	Animation maxIdleRight;
	Animation maxDead;
	Animation zap;
	Animation cubeFixed;
	TextureRegion cubeControlled;
	TextureRegion dispenser;
	Animation spawn;
	Animation dying;
	TextureRegion spikes;
	Animation rocket;
	Animation rocketExplosion;
	TextureRegion rocketPad;
	TextureRegion endDoor;
	TextureRegion movingSpikes;
	TextureRegion laser;
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
						}
					}
				}
				blocks[blockX][blockY] = cache.endCache();
			}
		}
		Gdx.app.debug("Timewrick", "blocks created");
	}

	private void createAnimations () {
		this.tile = new TextureRegion(new Texture(Gdx.files.internal("data/tile.png")), 0, 0, 20, 20);
		Texture maxTexture = new Texture(Gdx.files.internal("data/max.png"));
		TextureRegion[] split = new TextureRegion(maxTexture).split(20, 20)[0];
		TextureRegion[] mirror = new TextureRegion(maxTexture).split(20, 20)[0];
		for (TextureRegion region : mirror)
			region.flip(true, false);
		spikes = split[5];
		maxRight = new Animation(0.1f, split[0], split[1]);
		maxLeft = new Animation(0.1f, mirror[0], mirror[1]);
		maxJumpRight = new Animation(0.1f, split[2], split[3]);
		maxJumpLeft = new Animation(0.1f, mirror[2], mirror[3]);
		maxIdleRight = new Animation(0.5f, split[0], split[4]);
		maxIdleLeft = new Animation(0.5f, mirror[0], mirror[4]);
		maxDead = new Animation(0.2f, split[0]);

		split = new TextureRegion(maxTexture).split(20, 20)[1];
		spawn = new Animation(0.1f, split[4], split[3], split[2], split[1]);
		dying = new Animation(0.1f, split[1], split[2], split[3], split[4]);
		dispenser = split[5];

		split = new TextureRegion(maxTexture).split(20, 20)[2];
		rocketExplosion = new Animation(0.1f, split[0], split[1], split[2], split[3], split[4], split[4]);

		split = new TextureRegion(maxTexture).split(20, 20)[3];
		grass = new TextureRegion(split[0]);
		wood = new TextureRegion(split[1]);

		split = new TextureRegion(maxTexture).split(20, 20)[4];
		door = new TextureRegion(split[0]);

		split = new TextureRegion(maxTexture).split(20, 20)[5];
		endDoor = split[2];
	}

	float stateTime = 0;
	Vector3 lerpTarget = new Vector3();

	public void render (float deltaTime) {
			cam.position.lerp(lerpTarget.set(map.max.pos.x, map.max.pos.y, 0), 2f * deltaTime);
		cam.update();


		cache.setProjectionMatrix(cam.combined);
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
		batch.setProjectionMatrix(cam.combined);
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
