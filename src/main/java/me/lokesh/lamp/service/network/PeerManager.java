package me.lokesh.lamp.service.network;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.PeerOfflineEvent;
import me.lokesh.lamp.events.PeerOnlineEvent;
import me.lokesh.lamp.events.PeerStatusReceivedEvent;
import me.lokesh.lamp.events.PeerUpdateEvent;
import me.lokesh.lamp.service.api.Server;
import me.lokesh.lamp.service.models.Peer;
import me.lokesh.lamp.service.models.PeerStatus;
import me.lokesh.lamp.service.player.Track;
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

    public static final long TIMEOUT = 2 * 1000 * 1000 * 1000L;

    public PeerManager() {
        eventBus.register(this);
        peerStatusMap = FXCollections.observableHashMap();
        peerStatusTimeMap = new LinkedHashMap<>();

        executorService.scheduleAtFixedRate(new PeerStatusCheckRunnable(),
                TIMEOUT, TIMEOUT, TimeUnit.NANOSECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
        peerStatusMap.clear();
        peerStatusTimeMap.clear();
    }

    @Subscribe
    public void onPeerStatusReceivedEvent(PeerStatusReceivedEvent event) {
        PeerStatus peerStatus = event.getPeerStatus();
        Peer peer = peerStatus.getPeer();
        String ipAddress = peer.getIpAddress();

        if(peerStatusMap.get(ipAddress) == null) {
            peerStatusMap.put(ipAddress, peerStatus);
            eventBus.post(new PeerOnlineEvent(peer));
        } else {
            Peer oldPeer = peerStatusMap.get(ipAddress).getPeer();
            if (!oldPeer.getName().equals(peer.getName())){
                eventBus.post(new PeerUpdateEvent(oldPeer, peer));
            }
            peerStatusMap.replace(ipAddress, peerStatus);
        }


        long curTime = System.nanoTime();
        logger.debug("{} online at time {}", ipAddress, curTime);

        if(peerStatusTimeMap.get(ipAddress) == null) {
            peerStatusTimeMap.put(ipAddress, curTime);
        } else {
            peerStatusTimeMap.replace(ipAddress, curTime);
        }
    }

    public void playOnPeer(Peer peer, Track track) {
        try {
            String url = "http://" + peer.getIpAddress() + ":" + Server.PORT +
                    "/seed?url=" + URLEncoder.encode(track.getUrl(), "UTF-8") +
                    "&name=" + URLEncoder.encode(track.getName(), "UTF-8");

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

            Set<String> ipSet = peerStatusMap.keySet();
            Iterator<String> it = ipSet.iterator();
            while (it.hasNext()) {
                String ip = it.next();
                Long savedTime = peerStatusTimeMap.get(ip);

                if ((savedTime == null) || (curTime - savedTime > 2 * TIMEOUT)) {
                    logger.debug(ip + "went offline. savedTime={} dela ={}", savedTime, curTime - savedTime);

                    PeerStatus peerStatus = peerStatusMap.get(ip);
                    it.remove();
                    peerStatusTimeMap.remove(ip);
                    eventBus.post(new PeerOfflineEvent(peerStatus.getPeer()));
                } else  {
                    logger.debug("{} is still online!", ip);
                }
            }
        }
    }
}
