package me.lokesh.lamp.service.models;

/**
 * Created by lokesh.
 */
public class PeerStatus {
    public static final String ONLINE = "ONLINE";
    public static final String OFFLINE = "OFFLINE";

    Peer peer;
    boolean playing;
    String track;
    String playedBy;        //name of the node which started playback on this node
    String playedFrom;      //source node's name

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean isPlaying) {
        this.playing = isPlaying;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getPlayedBy() {
        return playedBy;
    }

    public void setPlayedBy(String playedBy) {
        this.playedBy = playedBy;
    }

    public String getPlayedFrom() {
        return playedFrom;
    }

    public void setPlayedFrom(String playedFrom) {
        this.playedFrom = playedFrom;
    }

    @Override
    public String toString() {
        return "NodeStatus{" +
                "node=" + peer +
                ", playing=" + playing +
                ", track='" + track + '\'' +
                ", playedBy='" + playedBy + '\'' +
                ", playedFrom='" + playedFrom + '\'' +
                '}';
    }
}
