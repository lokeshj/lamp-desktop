package me.lokesh.lamp.events;

import me.lokesh.lamp.service.models.Peer;

/**
 * Created by lokesh.
 */
public class PeerUpdateEvent {
    private Peer oldPeer;
    private Peer newPeer;

    public PeerUpdateEvent(Peer oldPeer, Peer newPeer) {
        this.oldPeer = oldPeer;
        this.newPeer = newPeer;
    }

    public Peer getOldPeer() {
        return oldPeer;
    }

    public void setOldPeer(Peer oldPeer) {
        this.oldPeer = oldPeer;
    }

    public Peer getNewPeer() {
        return newPeer;
    }

    public void setNewPeer(Peer newPeer) {
        this.newPeer = newPeer;
    }
}
