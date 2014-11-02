package me.lokesh.lamp.service;

import me.lokesh.lamp.service.api.Server;
import me.lokesh.lamp.service.network.Discoverer;
import me.lokesh.lamp.service.network.PeerManager;
import me.lokesh.lamp.service.player.Mp3Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lokesh.
 */
public class LAMPService {
    private static final Logger logger = LoggerFactory.getLogger(LAMPService.class);

    private static ExecutorService discoveryService = Executors.newSingleThreadExecutor();
    private static ExecutorService apiService = Executors.newSingleThreadExecutor();

    private static Mp3Player mp3Player;
    private static PeerManager peerManager;

    private static boolean started = false;

    public static void start() {
        logger.info("Starting lamp service");
        discoveryService.execute(new Discoverer());
        apiService.execute(new Server());
        mp3Player = new Mp3Player();
        peerManager = new PeerManager();
        started = true;
    }

    public static void stop() {
        logger.info("stopping lamp service");
        discoveryService.shutdownNow();
        apiService.shutdownNow();
        mp3Player.stop();
        peerManager.stop();
        started = false;
    }

    public static boolean isStarted() {
        return started;
    }

    public static PeerManager getPeerManager() {
        return peerManager;
    }
}
