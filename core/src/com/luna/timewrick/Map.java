
package com.luna.timewrick;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.luna.timewrick.screens.EndDoor;
import com.luna.timewrick.screens.GameScreen;

import java.util.ArrayList;
import java.util.Arrays;

public class Map {
	static int EMPTY = 0;
	static int DIRT = 0xffffff;
	static int GRASS = 0x0000ff;
	static int WOOD= 0xfd6400;
	static int START = 0xff0000;
	static int END = 0xff00ff;
	static int DISPENSER = 0xff0100;
	static int SPIKES = 0x00ff00;
	static int DOOR = 0xffff00;
	static float HALFSCREEN = 7.5f;

	float[] roomStart = new float[10], roomEnd = new float[10];
	static int[] solid = {DIRT, GRASS, WOOD};

	int[][] tiles;
	public Max max;
	public EndDoor endDoor;
	public ArrayList<Door> doors;

	public Map () {
		doors = new ArrayList<Door>();
		loadBinary();
	}

	private void loadBinary () {
		Pixmap pixmap = new Pixmap(Gdx.files.internal("data/levels.png"));
		tiles = new int[pixmap.getWidth()][pixmap.getHeight()];
		int room = 1, k = -1;
		roomStart[1] = HALFSCREEN+1;
		for (int y = 0; y < pixmap.getHeight(); y++) {
			for (int x = 0; x < pixmap.getWidth(); x++) {
				int pix = (pixmap.getPixel(x, y) >>> 8) & 0xffffff;
				if (match(pix, START)) {
					max = new Max(this, x, pixmap.getHeight() - 1 - y);
					max.state = Max.SPAWN;
				} else if (match(pix, END)) {
					endDoor = new EndDoor(x, pixmap.getHeight() - 1 - y);
				}else if (match(pix, DOOR)) {
					doors.add(new Door(x, pixmap.getHeight() - 1 - y));
					if(k==1) {
						roomStart[room] = x+HALFSCREEN+1;
					}
					else {
						roomEnd[room++] = x-HALFSCREEN;
					}
					k*=-1;
					System.out.println("Door: "+x);
				} else {
					// TODO: 15.11.15 THIS IS SO SHITTY
					tiles[x][y] = pix;
				}
			}
		}
		roomEnd[room] = pixmap.getWidth()-1-HALFSCREEN;

	}

	static boolean match (int src, int dst) {
		return src == dst;
	}

	public static boolean isSolid(int pix) {
		for (int i : solid) {
			if(match(pix, i)) return true;
		}
		return false;
	}

	public void update (float deltaTime) {
		max.update(deltaTime);
		if (max.state == Max.DEAD) GameScreen.gameOver = true;
	}

	public boolean isDeadly (int tileId) {
		return tileId == SPIKES;
	}
}
