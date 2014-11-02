package me.lokesh.lamp.events;

import me.lokesh.lamp.service.models.Peer;

/**
 * Created by lokesh.
 */
public class PeerOfflineEvent {
    private Peer peer;

    public PeerOfflineEvent(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
