package me.lokesh.lamp.service.models;

/**
 * Created by lokesh.
 */
public class PeerStatus {
    Peer peer;
    boolean playing;
    boolean libraryUpdated;
    String track;
    String playedBy;        //name of the node which started playback on this node
    String playedFrom;      //source node's name

    public PeerStatus() {
    }

    public PeerStatus(Peer peer, boolean playing, String track, boolean libraryUpdated) {
        this.peer = peer;
        this.playing = playing;
        this.libraryUpdated = libraryUpdated;
        this.track = track;
        this.playedBy = "";
        this.playedFrom = "";
    }

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

    public boolean isLibraryUpdated() {
        return libraryUpdated;
    }

    public void setLibraryUpdated(boolean libraryUpdated) {
        this.libraryUpdated = libraryUpdated;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PeerStatus) {
            PeerStatus other = (PeerStatus) obj;
            return peer.equals(other.getPeer()) &&
                    playing == other.isPlaying() &&
                    track.equals(other.getTrack()) &&
                    playedBy.equals(other.getPlayedBy()) &&
                    playedFrom.equals(other.getPlayedFrom()) &&
                    libraryUpdated == other.isLibraryUpdated();
        }
        return false;
    }

    @Override
    public String toString() {
        return "PeerStatus{" +
                "peer=" + peer +
                ", playing=" + playing +
                ", libraryUpdated=" + libraryUpdated +
                ", track='" + track + '\'' +
                ", playedBy='" + playedBy + '\'' +
                ", playedFrom='" + playedFrom + '\'' +
                '}';
    }
}
