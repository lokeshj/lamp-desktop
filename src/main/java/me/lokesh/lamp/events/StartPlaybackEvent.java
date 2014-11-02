package me.lokesh.lamp.events;

import me.lokesh.lamp.service.player.Track;

/**
 * Created by lokesh.
 */
public class StartPlaybackEvent {
    private Track track;

    public StartPlaybackEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
