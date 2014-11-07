package me.lokesh.lamp.service.models;

import me.lokesh.lamp.service.Config;

/**
 * Created by lokesh.
 */
public class Peer {
    public static final String WINDOWS = "WINDOWS";
    public static final String LINUX = "LINUX";
    public static final String MACOS = "MACOS";
    public static final String ANDROID = "ANDROID";
    public static final String IOS = "IOS";
    public static final String WINDOWS_PHONE = "WINDOWS_PHONE";
    public static final String UNKNOWN = "UNKNOWN";

    private String uuid;
    private String name;
    private String os;
    private String ipAddress;

    public Peer() {
        this.uuid = Config.getUuid();
    }

    public Peer(String name, String os, String ipAddress) {
        this.uuid = Config.getUuid();
        this.name = name;
        this.os = os;
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Peer) {
            Peer other = (Peer) obj;
            return other.getUuid().equals(this.getUuid());
        } else {
            return false;
        }
    }
}
