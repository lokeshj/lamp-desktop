package me.lokesh.lamp.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by lokesh.
 */
public class Config {
    private static Preferences prefs = Preferences.userRoot().node("/me/lokesh/lamp/config");

    private static boolean loaded = false;
    private static boolean registered = false;

    private static StringProperty deviceName = new SimpleStringProperty("");
    private static StringProperty musicFolderPath = new SimpleStringProperty("");

    private static final String KEY_DEVICE_NAME = "devicename";
    private static final String KEY_MUSIC_FOLDER_PATH = "musicFolderPath";

    public static void load() {
        String storedDevicename = prefs.get(KEY_DEVICE_NAME, "");
        String storedMusicFolderPath = prefs.get(KEY_MUSIC_FOLDER_PATH, "");

        if(storedDevicename.isEmpty() || storedMusicFolderPath.isEmpty()) {
            registered = false;
        } else {
            deviceName.setValue(storedDevicename);
            musicFolderPath.setValue(storedMusicFolderPath);

            registered = true;
        }

        loaded = true;
    }

    public static void save(String name, String musicfolderpath) {
        try {
            prefs.put(KEY_DEVICE_NAME, deviceName.getValue());
            prefs.put(KEY_MUSIC_FOLDER_PATH, musicfolderpath);
            prefs.flush();
            deviceName.setValue(name);
            musicFolderPath.setValue(musicfolderpath);

            loaded = true;
            registered = true;
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void saveDeviceName(String name) {
        save(name, musicFolderPath.getValue());
    }

    public static void saveMusicFolderPath(String musicFolderPath) {
        save(deviceName.getValue(), musicFolderPath);
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static StringProperty getDeviceName() {
        if(!loaded) {
            load();
        }
        return deviceName;
    }

    public static StringProperty getMusicFolderPathString() {
        if(!loaded) {
            load();
        }
        return musicFolderPath;
    }

    public static Path getMusicFolderPath() {
        if(!loaded) {
            load();
        }
        return new File(musicFolderPath.getValue()).toPath();
    }
}
