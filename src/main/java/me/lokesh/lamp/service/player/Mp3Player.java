package me.lokesh.lamp.service.player;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lokesh.
 */
public class Mp3Player {
    private final static Logger logger = LoggerFactory.getLogger(Mp3Player.class);
    private final EventBus eventBus = Shared.getEventBus();
    private final ExecutorService playbackExecutor = Executors.newSingleThreadExecutor();
    private AdvancedPlayer player;

    private int currentIndex;

    private List<Track> playlist;

    public Mp3Player() {
        eventBus.register(this);
        playlist = new ArrayList<>(20);
        currentIndex = -1;
    }

    public void play(Track track){
        if(player != null) {
            player.close();
        }

        clearPlaylist();
        addToPlaylist(track);
        playAt(0);
    }

    public void next() {
        if(playlistHasMore()) {
            close();
            playAt(currentIndex + 1);
        }
    }

    public void previous() {
        if(currentIndex > 0) {
            close();
            playAt(currentIndex - 1);
        }
    }

    public void addToPlaylist(Track track) {
        playlist.add(track);
    }

    private void clearPlaylist() {
        playlist.clear();
        currentIndex = -1;
    }

    private void playAt(int index) {
        currentIndex = index;
        playbackExecutor.execute(new PlayerRunnable());
    }

    public boolean isPlaying() {
        return player != null;
    }

    public Track getCurrentTrack() {
        if(playlist.size() > 0) {
            return playlist.get(currentIndex);
        } else {
            return null;
        }
    }

    public void stopPlayback() {
        if (player != null) {
            player.stop();
        }
    }

    public void close() {
        logger.info("Stopping Audio Player");
        if (player != null) {
            player.close();
        }

        playbackExecutor.shutdownNow();
    }

    private boolean playlistHasMore() {
        return playlist.size() > currentIndex + 1;
    }

    @Subscribe
    public void onStartPlaybackEvent(StartPlaybackEvent event) {
        play(event.getTrack());
    }

    @Subscribe
    public void onStopPlaybackEvent(StopPlaybackEvent event) {
        stopPlayback();
    }

    @Subscribe
    public void onAddToPlaybackEvent(AddToPlaylistEvent event) {
        addToPlaylist(event.getTrack());
    }

    @Subscribe
    public void onPlaybackFinish(PlaybackStoppedEvent event) {
        if(playlistHasMore()) {
            next();
        }
    }

    private class PlayerRunnable extends PlaybackListener implements Runnable {

        public void playbackStarted(PlaybackEvent playbackEvent) {
            logger.info("playing {}", playlist.get(currentIndex));
            eventBus.post(new PlaybackStartedEvent(playlist.get(currentIndex)));
        }

        public void playbackFinished(PlaybackEvent playbackEvent) {
            eventBus.post(new PlaybackStoppedEvent());
            player = null;
        }

        @Override
        public void run() {
            String urlAsString = playlist.get(currentIndex).getUrl();
            logger.info("Attempting playback of url {}", urlAsString);
            try {
                player = new AdvancedPlayer(new URL(urlAsString).openStream());
                player.setPlayBackListener(this);
                player.play();
            } catch (JavaLayerException e) {
                logger.error("error in playback of {}, error=", urlAsString, e);
                player = null;
                eventBus.post(new PlaybackStoppedEvent());
            } catch (IOException e) {
                logger.error("error opening file for playback path={}, error={}", urlAsString, e);
            }
        }
    }
}
