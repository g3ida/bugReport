package com.g3ida.bugreport.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.g3ida.bugreport.BugReport;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Bug Report";
		config.width = 800;
		config.height = 480;
		config.resizable = true;
		config.foregroundFPS = 60;
		new LwjglApplication(new BugReport(), config);
	}
}
