package me.lokesh.lamp.events;

import me.lokesh.lamp.service.player.Track;

/**
 * Created by lokesh.
 */
public class StartRemotePlaybackEvent {
    private String ipAddress;
    private Track track;

    public StartRemotePlaybackEvent(String ipAddress, Track track) {
        this.ipAddress = ipAddress;
        this.track = track;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
