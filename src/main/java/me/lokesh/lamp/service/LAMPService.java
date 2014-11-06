package me.lokesh.lamp.service;

import me.lokesh.lamp.service.api.Server;
import me.lokesh.lamp.service.network.Discoverer;
import me.lokesh.lamp.service.network.PeerManager;
import me.lokesh.lamp.service.player.Mp3Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lokesh.
 */
public class LAMPService {
    private static final Logger logger = LoggerFactory.getLogger(LAMPService.class);

    private static ScheduledExecutorService discoveryService = Executors.newSingleThreadScheduledExecutor();
    private static ExecutorService apiService = Executors.newSingleThreadExecutor();

    private static Mp3Player mp3Player;
    private static PeerManager peerManager;
    private static Discoverer discoverer;

    private static boolean started = false;

    public static void start() {
        logger.info("Starting lamp service");
        mp3Player = new Mp3Player();
        peerManager = new PeerManager();
        discoverer = new Discoverer();
        discoveryService.scheduleAtFixedRate(discoverer, 500, 100, TimeUnit.MILLISECONDS);
        apiService.execute(new Server());
        started = true;
        logger.info("lamp service started");
    }

    public static void stop() {
        logger.info("stopping lamp service");
        discoveryService.shutdownNow();
        apiService.shutdownNow();
        mp3Player.close();
        peerManager.stop();
        started = false;
        logger.info("lamp service stopped");
    }

    public static boolean isStarted() {
        return started;
    }

    public static PeerManager getPeerManager() {
        return peerManager;
    }
    public static Mp3Player getMp3Player() {
        return mp3Player;
    }
    public static Discoverer getDiscoverer() {
        return discoverer;
    }
}
