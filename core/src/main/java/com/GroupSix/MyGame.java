package com.GroupSix;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MyGame extends Game {
    public Music music;

    @Override
    public void create() {
        // Load music here so it's available to all screens
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);

        // Start with the menu screen
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        music.dispose();
    }
}
