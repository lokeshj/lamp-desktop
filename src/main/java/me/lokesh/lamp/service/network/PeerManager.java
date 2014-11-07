package me.lokesh.lamp.service.network;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.*;
import me.lokesh.lamp.service.api.Server;
import me.lokesh.lamp.service.models.Peer;
import me.lokesh.lamp.service.models.PeerStatus;
import me.lokesh.lamp.service.utils.HttpAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lokesh.
 */
public class PeerManager {
    private static final Logger logger = LoggerFactory.getLogger(PeerManager.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final EventBus eventBus = Shared.getEventBus();

    private ObservableMap<String, PeerStatus> peerStatusMap;
    private HashMap<String, Long> peerStatusTimeMap;

    public static final long TIMEOUT = 3 * 1000 * 1000 * 1000L;

    public PeerManager() {
        eventBus.register(this);
        peerStatusMap = FXCollections.observableHashMap();
        peerStatusTimeMap = new LinkedHashMap<>();

        executorService.scheduleAtFixedRate(new PeerStatusCheckRunnable(),
                2 * TIMEOUT, TIMEOUT, TimeUnit.NANOSECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
        peerStatusMap.clear();
        peerStatusTimeMap.clear();
    }

    public ObservableMap<String, PeerStatus> getPeerStatusMap() {
        return peerStatusMap;
    }

    @Subscribe
    public void onPeerStatusReceivedEvent(PeerStatusReceivedEvent event) {
        long curTime = System.nanoTime();

        PeerStatus peerStatus = event.getPeerStatus();
        Peer peer = peerStatus.getPeer();
        logger.debug("{} online at time {}", peer, curTime);

        String uuid = peer.getUuid();
        if(peerStatusTimeMap.get(uuid) == null) {
            peerStatusTimeMap.put(uuid, curTime);
        } else {
            peerStatusTimeMap.replace(uuid, curTime);
        }

        if(peerStatusMap.get(uuid) == null) {
            peerStatusMap.put(uuid, peerStatus);
            eventBus.post(new PeerOnlineEvent(peer));
        } else {
            PeerStatus oldStatus = peerStatusMap.get(uuid);
            Peer oldPeer = peerStatusMap.get(uuid).getPeer();

            if (!oldStatus.equals(peerStatus)) {
                eventBus.post(new PeerUpdateEvent(oldPeer, peer, peerStatus.isLibraryUpdated()));
            }
            peerStatusMap.replace(uuid, peerStatus);
        }
    }

    @Subscribe
    public void onStartRemotePlaybackEvent(StartRemotePlaybackEvent event) {
        try {
            String url = "http://" + event.getIpAddress() + ":" + Server.PORT +
                    "/seed?url=" + URLEncoder.encode(event.getTrack().getUrl(), "UTF-8") +
                    "&name=" + URLEncoder.encode(event.getTrack().getName(), "UTF-8");

            String response = HttpAgent.get(url);
            logger.info(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class PeerStatusCheckRunnable implements Runnable {
        @Override
        public void run() {
            long curTime = System.nanoTime();
            logger.debug("peer status check running @ {}", curTime);

            Set<String> uuidSet = peerStatusMap.keySet();
            Iterator<String> it = uuidSet.iterator();
            while (it.hasNext()) {
                String uuid = it.next();
                Long savedTime = peerStatusTimeMap.get(uuid);

                if ((savedTime == null) || (curTime - savedTime > 2 * TIMEOUT)) {
                    logger.debug(uuid + "went offline. savedTime={}, curTime ={}", savedTime, curTime);

                    PeerStatus peerStatus = peerStatusMap.get(uuid);
                    it.remove();
                    peerStatusTimeMap.remove(uuid);
                    eventBus.post(new PeerOfflineEvent(peerStatus.getPeer()));
                } else  {
                    logger.debug("{} is still online!", uuid);
                }
            }
        }
    }
}
