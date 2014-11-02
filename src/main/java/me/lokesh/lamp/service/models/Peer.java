package me.lokesh.lamp.service.models;

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

    String name;
    String os;
    String ipAddress;

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

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", os='" + os + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Peer) {
            Peer other = (Peer) obj;
            return other.getIpAddress().equals(this.getIpAddress());
        } else {
            return false;
        }
    }
}
