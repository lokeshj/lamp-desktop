package me.lokesh.lamp.events;

import me.lokesh.lamp.service.player.Track;

/**
 * Created by lokesh.
 */
public class AddToPlaylistEvent {
    private Track track;

    public AddToPlaylistEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
