package me.lokesh.lamp.service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by lokesh.
 */
public class Config {
    private static Preferences prefs = Preferences.userRoot().node("/me/lokesh/lamp/config");
    private static String uuid;

    private static boolean loaded = false;
    private static BooleanProperty registered = new SimpleBooleanProperty(false);

    private static StringProperty deviceName = new SimpleStringProperty("");
    private static StringProperty musicFolderPath = new SimpleStringProperty("");
    private static boolean musicFolderUpdated = false;

    private static final String KEY_DEVICE_NAME = "devicename";
    private static final String KEY_MUSIC_FOLDER_PATH = "musicFolderPath";
    private static final String KEY_UUID = "uuid";

    public static void load() {
        uuid = prefs.get(KEY_UUID, "");
        String storedDevicename = prefs.get(KEY_DEVICE_NAME, "");
        String storedMusicFolderPath = prefs.get(KEY_MUSIC_FOLDER_PATH, "");

        if (storedDevicename.isEmpty() || storedMusicFolderPath.isEmpty()) {
            registered.setValue(false);
        } else {
            deviceName.setValue(storedDevicename);
            musicFolderPath.setValue(storedMusicFolderPath);

            registered.setValue(true);
        }

        loaded = true;
    }

    public static void save(String name, String musicfolderpath) {
        try {
            if (!prefs.get(KEY_MUSIC_FOLDER_PATH, "").equals(musicfolderpath)) {
                musicFolderUpdated = true;
            }
            prefs.put(KEY_DEVICE_NAME, deviceName.getValue());
            prefs.put(KEY_MUSIC_FOLDER_PATH, musicfolderpath);
            prefs.put(KEY_UUID, uuid);
            prefs.flush();
            deviceName.setValue(name);
            musicFolderPath.setValue(musicfolderpath);

            loaded = true;
            registered.setValue(true);
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static BooleanProperty getRegisteredProperty() {
        return registered;
    }

    public static boolean isRegistered() {
        return registered.getValue();
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

    public static String getUuid() {
        if(uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
        }

        return uuid;
    }

    public static boolean isMusicFolderUpdated() {
        if(musicFolderUpdated) {
            musicFolderUpdated = false;
            return true;
        }
        return false;
    }
}
