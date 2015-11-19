
package com.luna.timewrick;

import com.luna.timewrick.screens.MainMenu;
import com.badlogic.gdx.Game;

public class Timewrick extends Game {
	@Override
	public void create () {
		setScreen(new MainMenu(this));
	}
}
