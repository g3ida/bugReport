package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;

/**
 * Created by g3ida on 8/5/2017.
 */

public class Settings {
    public static final int NUM_LEVELS = 30;
    public static int levelsUnlocked = 1;
    public static int levelsWon = 0;
    public static boolean[] isWonLevel = new boolean[NUM_LEVELS];
    public static boolean[] isUnlockedLevel = new boolean[NUM_LEVELS];
    public static boolean[] isLevelDiamondTaken = new boolean[NUM_LEVELS];


    public static int PIXELS_PER_METER = 65;

    public static final float SCREEN_WIDTH = 1024.f;
    public static final float SCREEN_HEIGHT = 614.f;

    public static boolean isSilent = false;
    public static int menuCurrentPage = 0;

    public static final int NUM_PLAYERS = 5;
    public static boolean[] isUnlockedPlayer = new boolean[NUM_PLAYERS];
    public static int currentPlayer = 0;
    public static  String[] playerNames = new String[NUM_PLAYERS];

    public static Sound buttonSFX;
    public static Sound shakeSFX;
    public static Sound button2SFX;
    public static Sound buttonErrorSFX;
    public static Sound elasticSFX;
    public static Sound cancelSFX;
    public static Sound smileSFX;
    public static Sound diamondCaptureSFX;

    public static final String PREFERENCES_NAME = new String("BugReportPreferences");


    //game stats
    public static int numJumps = 0;
    public static int numDeaths = 0;
    public static double cuurentDelay = 0;
    public static int secondsPlayed = 0;
    public static int numRestarts = 0;
    public static int numDiamonds = 0;
    public static int overallDiamonds = 0;
    public static int numSmiles = 0;

    public static int numTitleShakes = 0;

    //unique to level 29, I know this is a big mess...
    public static boolean level29FirstTimeEntred = true;
    //public static int level29FirstPlayer = 0;
    public static int level29SecondPlayer = 0;


    public static final int NUM_ACHIEVEMENTS = 12;
    public static boolean[] isAchievementsUnlocked = new boolean[NUM_ACHIEVEMENTS];
    public static int achievementsUnlocked = 0;

    public static int getEllapsedTimeInSeconds() {
        secondsPlayed += (int) cuurentDelay;
        cuurentDelay = cuurentDelay - Math.floor(cuurentDelay);
        if(secondsPlayed > 3600 && isAchievementsUnlocked[0]==false) {
            EventHandler.get().notifyObservers(new Event(Event.Type.ONE_HOUR_ELLAPSED));
        }
        return secondsPlayed;
    }

    public static void load() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        isSilent = preferences.getBoolean("isSilent", isSilent);
        levelsUnlocked = preferences.getInteger("levelsUnlocked", levelsUnlocked);
        menuCurrentPage = preferences.getInteger("menuCurrentPage", menuCurrentPage);
        currentPlayer = preferences.getInteger("currentPlayer", currentPlayer);
        numDeaths = preferences.getInteger("numDeaths", numDeaths);
        numJumps = preferences.getInteger("numJumps", numJumps);
        secondsPlayed = preferences.getInteger("secondsPlayed", secondsPlayed);
        numRestarts = preferences.getInteger("numRestarts", numRestarts);
        levelsWon = preferences.getInteger("levelsWon", levelsWon);
        achievementsUnlocked = preferences.getInteger("achievementsUnlocked", achievementsUnlocked);
        numDiamonds = preferences.getInteger("numDiamonds", numDiamonds);
        overallDiamonds = preferences.getInteger("overallDiamonds", overallDiamonds);
        numSmiles = preferences.getInteger("numSmiles", numSmiles);
        numTitleShakes = preferences.getInteger("numTitleShakes", numTitleShakes);

        for(int i=0; i< NUM_LEVELS; ++i){
            isWonLevel[i] = preferences.getBoolean("isWonLevel" + i, isWonLevel[i]);
            isUnlockedLevel[i] = preferences.getBoolean("isUnlockedLevel" + i, isUnlockedLevel[i]);
            isLevelDiamondTaken[i] = preferences.getBoolean("isLevelDiamondTaken" + i, isLevelDiamondTaken[i]);
        }

        for(int i=0; i< NUM_PLAYERS; ++i){
            isUnlockedPlayer[i] = preferences.getBoolean("isUnlocked" + i, isUnlockedPlayer[i]);
        }

        for(int i=0; i< NUM_ACHIEVEMENTS; ++i){
            isAchievementsUnlocked[i] = preferences.getBoolean("isAchievementsUnlocked" + i, isAchievementsUnlocked[i]);
        }


        level29FirstTimeEntred = preferences.getBoolean("level29FirstTimeEntred", level29FirstTimeEntred);
        //level29FirstPlayer = preferences.getInteger("level29FirstPlayer", level29FirstPlayer);
        level29SecondPlayer = preferences.getInteger("level29SecondPlayer", level29SecondPlayer);

    }

    public static void save() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        preferences.putBoolean("isSilent", isSilent);
        preferences.putInteger("levelsUnlocked", levelsUnlocked);
        preferences.putInteger("menuCurrentPage", menuCurrentPage);
        preferences.putInteger("currentPlayer", currentPlayer);
        preferences.putInteger("numDeaths", numDeaths);
        preferences.putInteger("numJumps", numJumps);
        preferences.putInteger("secondsPlayed", getEllapsedTimeInSeconds());
        preferences.putInteger("numRestarts", numRestarts);
        preferences.putInteger("levelsWon", levelsWon);
        preferences.putInteger("achievementsUnlocked", achievementsUnlocked);
        preferences.putInteger("numDiamonds", numDiamonds);
        preferences.putInteger("overallDiamonds", overallDiamonds);
        preferences.putInteger("numSmiles", numSmiles);
        preferences.putInteger("numTitleShakes", numTitleShakes);

        for(int i=0; i< NUM_ACHIEVEMENTS; ++i){
            preferences.putBoolean("isAchievementsUnlocked" + i, isAchievementsUnlocked[i]);
        }

        for(int i=0; i< NUM_PLAYERS; ++i){
            preferences.putBoolean("isUnlocked" + i, isUnlockedPlayer[i]);
        }

        for(int i=0; i< NUM_LEVELS; ++i){
            preferences.putBoolean("isWonLevel" + i, isWonLevel[i]);
            preferences.putBoolean("isUnlockedLevel" + i, isUnlockedLevel[i]);
            preferences.putBoolean("isLevelDiamondTaken" + i, isLevelDiamondTaken[i]);

        }

        preferences.putBoolean("level29FirstTimeEntred", level29FirstTimeEntred);
        //preferences.putInteger("level29FirstPlayer", level29FirstPlayer);
        preferences.putInteger("level29SecondPlayer", level29SecondPlayer);

        preferences.flush();
    }

    public static void dispose() {
        buttonSFX.dispose();
        shakeSFX.dispose();
        button2SFX.dispose();
        buttonErrorSFX.dispose();
        elasticSFX.dispose();
        cancelSFX.dispose();
        smileSFX.dispose();
        diamondCaptureSFX.dispose();
    }

    public static void init() {
        buttonSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/button.ogg"));
        shakeSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/shake.ogg"));
        button2SFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/button2.ogg"));
        buttonErrorSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/buttonError.ogg"));
        elasticSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/elastic.ogg"));
        cancelSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/cancel.ogg"));
        smileSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/smile.ogg"));
        diamondCaptureSFX = Gdx.audio.newSound(Gdx.files.internal("data/sounds/diamondCapture.ogg"));

        for(int i=0; i< NUM_PLAYERS; ++i){
            isUnlockedPlayer[i] = false;
        }
        isUnlockedPlayer[0] = true;

        for(int i=0; i< NUM_LEVELS; ++i){
            isUnlockedLevel[i] = false;
            isWonLevel[i] = false;
            isLevelDiamondTaken[i] = false;

        }
        isUnlockedLevel[0] = true;

        for(int i=0; i< NUM_ACHIEVEMENTS; ++i){
            isAchievementsUnlocked[i] = false;
        }

        for(int i=0; i< NUM_PLAYERS; i++) {
            playerNames[i] = "p"+i;
        }
    }
}
