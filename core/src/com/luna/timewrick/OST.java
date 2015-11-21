package com.luna.timewrick;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Created by luna on 21.11.15.
 */
public class OST {
    public static Music music;

    public void playTheme() {
        music = Gdx.audio.newMusic(Gdx.files.internal("data/MAX.mp3"));
        music.setLooping(true);
        music.setVolume(0.2f);
        music.play();
        // TODO: 21.11.15 make settings.musicSwitcher
    }
}
