package com.g3ida.bugreport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;


class Config {
    public Config() {}

    public float playerOffsetX = 0.f;
    public float playerOffsetY = 0.f;
    public float gameSpeed = 1.f;

    public boolean flippedWorldH = false;
    public boolean flippedWorldW = false;
    //public boolean winReversed = false;
    public boolean buggyJump = false;
    public boolean infiniteWalk = false;
    public boolean winCrash = false;
    public boolean gravityReversed = false;
    public boolean flippedButtonsWithPlatform = false;
    public boolean drawLag = false;
    public float timelag = 0.f;
    public boolean drawError = false;
    public boolean allwaysJump = false;

    public boolean randomPositionX = false;
    public boolean randomPositionY = false;

    public boolean randombatchColor = false;
    public boolean restartAndJumpReversed = false;
    public boolean drawX = false;
    public boolean restartFixedBox = false;

    public boolean player2ShouldWin = true;

    public boolean randomGameSpeed = false;



    public boolean doTimeout = false;
    public float timeout = -1.f;
}

public class LevelSettings {

    public static String quotations[];
    public static String aurhors[];
    public static Config config[];
    public static int currentLevel = 0;

    public static boolean isConfigurationLoaded() { return (config != null);}

    public static void loadConfiguration() {

        config = new Config[Settings.NUM_LEVELS];
        for(int i=0; i< Settings.NUM_LEVELS; i++) {
            config[i] = new Config();
        }

        XmlReader reader = new XmlReader();
        XmlReader.Element root;
        try {
            root = reader.parse(Gdx.files.internal("data/Levels/conf.xml"));
            for(int i=0; i<root.getChildCount(); i++) {
                XmlReader.Element child = root.getChild(i);

                int level = child.getIntAttribute("id")-1;

                config[level].playerOffsetX = child.getFloatAttribute("playerOffsetX", 0.f);
                config[level].playerOffsetY = child.getFloatAttribute("playerOffsetY", 0.f);
                config[level].gameSpeed = child.getFloatAttribute("gameSpeed", 1.f);

                config[level].flippedWorldH = child.getBooleanAttribute("flippedWorldH", false);
                config[level].flippedWorldW = child.getBooleanAttribute("flippedWorldW", false);
                config[level].buggyJump = child.getBooleanAttribute("buggyJump", false);
                config[level].infiniteWalk = child.getBooleanAttribute("infiniteWalk", false);
                config[level].winCrash = child.getBooleanAttribute("winCrash", false);
                config[level].gravityReversed = child.getBooleanAttribute("gravityReversed", false);
                config[level].flippedButtonsWithPlatform = root.getBooleanAttribute("flippedButtonsWithPlatform", false);
                config[level].drawLag = child.getBooleanAttribute("drawLag", false);
                config[level].timelag = child.getFloatAttribute("timelag", 0.f);
                config[level].drawError = child.getBooleanAttribute("drawError", false);
                config[level].allwaysJump = child.getBooleanAttribute("allwaysJump", false);

                config[level].randomPositionX = child.getBooleanAttribute("randomPositionX", false);
                config[level].randomPositionY = child.getBooleanAttribute("randomPositionY", false);

                config[level].randombatchColor = child.getBooleanAttribute("randombatchColor", false);
                config[level].restartAndJumpReversed = child.getBooleanAttribute("restartAndJumpReversed", false);
                config[level].drawX = child.getBooleanAttribute("drawX", false);
                config[level].restartFixedBox = child.getBooleanAttribute("restartFixedBox", false);
                config[level].player2ShouldWin = child.getBooleanAttribute("player2ShouldWin", false);
                config[level].randomGameSpeed = child.getBooleanAttribute("randomGameSpeed", false);

                config[level].timeout = child.getFloatAttribute("timeout", -1.f);
                config[level].doTimeout =  (config[level].timeout != -1.f);


            }
        } catch (java.io.IOException e) {
            config = null;
        }
    }

    public static boolean isQuotationsLoaded() {
        return (quotations != null && aurhors != null);
    }

    public static void deleteQuotations() {
        quotations = null;
        aurhors = null;
    }

    public static void loadQuotations() {
        quotations = new String[Settings.NUM_LEVELS];
        aurhors = new String[Settings.NUM_LEVELS];

        XmlReader reader = new XmlReader();
        XmlReader.Element root;
        try {
            root = reader.parse(Gdx.files.internal("data/Levels/quotations.xml"));
            for(int i=0; i<root.getChildCount(); i++) {
                XmlReader.Element child = root.getChild(i);
                int level = child.getIntAttribute("level")-1;
                if(level < Settings.NUM_LEVELS) {
                    quotations[level] = child.getText();
                    aurhors[level] = child.getAttribute("author", "Anonymous");
                }
            }
        } catch (java.io.IOException e) {
            quotations = null;
            aurhors = null;
        }
    }
}
