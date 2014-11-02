package me.lokesh.lamp.events;

import me.lokesh.lamp.service.models.PeerStatus;

/**
 * Created by lokesh.
 */
public class PeerStatusReceivedEvent {
    private PeerStatus peerStatus;

    public PeerStatusReceivedEvent(PeerStatus peerStatus) {
        this.peerStatus = peerStatus;
    }

    public PeerStatus getPeerStatus() {
        return peerStatus;
    }

    public void setPeerStatus(PeerStatus peerStatus) {
        this.peerStatus = peerStatus;
    }
}
