package com.luna.timewrick;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Created by luna on 21.11.15.
 */
public class OST {
    public static Music music = Gdx.audio.newMusic(Gdx.files.internal("data/MAX.mp3"));;

    public void playTheme() {
        music.setLooping(true);
        music.setVolume(0.2f);
        music.play();
        // TODO: 21.11.15 make settings.musicSwitcher
    }

    public void stopTheme() {
        music.stop();
    }
}
