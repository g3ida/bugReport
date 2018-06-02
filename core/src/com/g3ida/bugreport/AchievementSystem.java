package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by g3ida on 1/20/2018.
 */

public class AchievementSystem implements Observer {

    private static Sound _achievementSFX;

    private static AchievementSystem _instance;
    private static TextureAtlas _atlas;

    public static TextureAtlas.AtlasRegion _plusDiamondRegion;
    public static boolean _drawAchievement = false;
    public static TextureAtlas.AtlasRegion _achievementRegion;
    public static ConcurrentLinkedQueue<TextureAtlas.AtlasRegion> _achievementRegions = new ConcurrentLinkedQueue<TextureAtlas.AtlasRegion>();
    public static Appear _appearAchievement;

    private AchievementSystem() {}


    public TextureAtlas getAtlas() {
        return _atlas;
    }

    public static final AchievementSystem get() {
        if (_instance == null) {
            _instance = new AchievementSystem();
            EventHandler.get().addObserver(AchievementSystem.get());
            _achievementSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/achievementUnlocked.ogg"));
            AudioManager.get().attachSound(_achievementSFX, Event.Type.ACHIEVEMENT_SHOWN);
            _atlas = new TextureAtlas(Gdx.files.internal("data/graphics/achievements.pack"));
            _plusDiamondRegion = _atlas.findRegion("plusDiamond");
        }
        return _instance;
    }

    @Override
    public void update(Observable observable, Object o) {

        if(o == null) return;
        if (!(o instanceof Event)) return;
        Event e = (Event) o;

        switch(e.type) {
            case LEVEL_CLEAR:
            case ACHIEVEMENT_SHOWN:
                //Settings.overallDiamonds++;
                if(Settings.numDiamonds == 10) {
                    if(!Settings.isAchievementsUnlocked[5]) {
                        Settings.isAchievementsUnlocked[5] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 6;
                        EventHandler.get().notifyObservers(e2);
                    }
                } else if(Settings.numDiamonds == 25) {
                    if(!Settings.isAchievementsUnlocked[6]) {
                        Settings.isAchievementsUnlocked[6] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 7;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
              //  break;

            //case LEVEL_CLEAR:
                if(Settings.levelsWon == 10) {
                    if(!Settings.isAchievementsUnlocked[3]) {
                        Settings.isAchievementsUnlocked[3] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 4;
                        EventHandler.get().notifyObservers(e2);
                    }
                } else if(Settings.levelsWon == Settings.NUM_LEVELS) {
                    if(!Settings.isAchievementsUnlocked[4]) {
                        Settings.isAchievementsUnlocked[4] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 5;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
                break;

            case PLAYER_PURSHASE:
                int count = 0;
                for(int i=0; i<Settings.NUM_PLAYERS; i++) {
                    if(Settings.isUnlockedPlayer[i])
                        count++;
                }
                if(count == Settings.NUM_PLAYERS) {
                    if(!Settings.isAchievementsUnlocked[8]) {
                        Settings.isAchievementsUnlocked[8] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 9;
                        EventHandler.get().notifyObservers(e2);
                    }
                } else if(count == 2) {
                    if(!Settings.isAchievementsUnlocked[7]) {
                        Settings.isAchievementsUnlocked[7] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 8;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
                break;

            case PLAYER_JUMPED:
                if(Settings.numJumps == 500) {
                    if(!Settings.isAchievementsUnlocked[1]) {
                        Settings.isAchievementsUnlocked[1] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 2;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
                break;

            case PLAYER_DEAD:
                if(Settings.numDeaths == 200) {
                    if(!Settings.isAchievementsUnlocked[2]) {
                        Settings.isAchievementsUnlocked[2] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 3;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
                break;

            case ONE_HOUR_ELLAPSED:
                if(!Settings.isAchievementsUnlocked[0]) {
                    Settings.isAchievementsUnlocked[0] = true;
                    Settings.achievementsUnlocked++;
                    Event e2 = new Event();
                    e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                    e2.info.achievementNum = 1;
                    EventHandler.get().notifyObservers(e2);
                }
                break;

            case SMILING_PLATFORM_TRGGERED :
                if(Settings.numSmiles == 100) {
                    if(!Settings.isAchievementsUnlocked[9]) {
                        Settings.isAchievementsUnlocked[9] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 10;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
                break;
            case TITLE_SHAKED:
                Settings.numTitleShakes++;
                if(Settings.numTitleShakes == 10) {
                    if(!Settings.isAchievementsUnlocked[11]) {
                        Settings.isAchievementsUnlocked[11] = true;
                        Settings.achievementsUnlocked++;
                        Event e2 = new Event();
                        e2.type = Event.Type.ACHIVEMENT_UNLOCKED;
                        e2.info.achievementNum = 12;
                        EventHandler.get().notifyObservers(e2);
                    }
                }
                break;
        }
    }
}
