
package com.luna.timewrick.screens;

import com.luna.timewrick.MapRenderer;
import com.luna.timewrick.Map;
import com.luna.timewrick.OST;
import com.luna.timewrick.OnscreenControlRenderer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;

public class GameScreen extends TimewrickScreen {
	Map map;
	MapRenderer renderer;
	public static boolean gameOver = false;
	OnscreenControlRenderer controlRenderer;
	public GameScreen (Game game) {
		super(game);
	}

	@Override
	public void show () {
		map = new Map();
		renderer = new MapRenderer(map);
		controlRenderer = new OnscreenControlRenderer(map);
	}

	@Override
	public void render (float delta) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
        map.update(delta);
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.render(delta);
		controlRenderer.render();

		if (map.max.bounds.overlaps(map.endDoor.bounds)||gameOver) {
			game.setScreen(new GameOverScreen(game));
		}

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			game.setScreen(new MainMenu(game));
		}
	}

	@Override
	public void hide () {
		Gdx.app.debug("Timewrick", "dispose game screen");
		renderer.dispose();
		controlRenderer.dispose();
	}
}
