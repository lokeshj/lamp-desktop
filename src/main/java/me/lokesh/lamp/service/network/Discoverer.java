package me.lokesh.lamp.service.network;

import com.google.common.eventbus.EventBus;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.NetworkErrorEvent;
import me.lokesh.lamp.events.NetworkSuccessEvent;
import me.lokesh.lamp.events.PeerStatusReceivedEvent;
import me.lokesh.lamp.service.Config;
import me.lokesh.lamp.service.models.Peer;
import me.lokesh.lamp.service.models.PeerStatus;
import me.lokesh.lamp.service.utils.JsonHandler;
import me.lokesh.lamp.service.utils.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by lokesh.
 */
public class Discoverer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Discoverer.class);
    private final EventBus eventBus = Shared.getEventBus();

    private static final int DISCOVERY_PORT = 15491;
    private static final int TIMEOUT_MS = 500;

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            while (true) {
                try {
                    broadcastStatus(socket);
                    eventBus.post(new NetworkSuccessEvent());
                } catch (Exception e) {
                    logger.error("error in creating/sending packet",e);
                    eventBus.post(new NetworkErrorEvent());
                } finally {
                    receiveStatus(socket);
                    Thread.sleep(200);
                }
            }

        } catch (InterruptedException e) {
            logger.info("Discoverer Stopped!");
        } catch (IOException e) {
            logger.error("Could not send discovery request", e);
        }
    }

    private void broadcastStatus(DatagramSocket socket) throws Exception{
        socket.send(createPacket());
    }

    private static DatagramPacket createPacket() throws Exception {
        InetAddress broadcastAddress = getBroadcastAddress();
        if (broadcastAddress == null) {
            throw new Exception("Network Disconnected");
        }

        Peer peer = new Peer();
        peer.setIpAddress(SystemProperties.getIPAddress());
        peer.setName(Config.getDeviceName().getValue());
        if (SystemProperties.isOSWindows()) {
            peer.setOs(Peer.WINDOWS);
        } else if (SystemProperties.isOSLinux()) {
            peer.setOs(Peer.LINUX);
        } else if (SystemProperties.isOSMac()) {
            peer.setOs(Peer.MACOS);
        } else if (SystemProperties.isOSAndroid()) {
            peer.setOs(Peer.ANDROID);
        } else {
            peer.setOs(Peer.UNKNOWN);
        }

        PeerStatus peerStatus = new PeerStatus();
        peerStatus.setPlaying(false);
        peerStatus.setPlayedBy("");
        peerStatus.setPlayedFrom("");
        peerStatus.setTrack("");
        peerStatus.setPeer(peer);

        String data = JsonHandler.stringify(peerStatus);
        logger.trace("created discovery packet with data: " + data);

        return new DatagramPacket(data.getBytes(), data.length(),
                broadcastAddress, DISCOVERY_PORT);
    }

    public static InetAddress getBroadcastAddress() {
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback()) {
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast != null) {
                            return broadcast;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void receiveStatus(DatagramSocket socket) {
        int loopLength = 100;   //just for more receives than broadcast per iteration and also reducing cpu load
        while (loopLength>0) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String s = new String(packet.getData(), 0, packet.getLength());

                PeerStatus peerStatus = JsonHandler.parse(s, PeerStatus.class);

                if (!peerStatus.getPeer().getIpAddress().equals(SystemProperties.getIPAddress())) {
                    logger.debug("received nodestatus: " + peerStatus);
                    eventBus.post(new PeerStatusReceivedEvent(peerStatus));
                } else {
                    logger.trace("received own presence packet");
                }

            } catch (IOException e) {
                logger.trace("timeout waiting for status packet");
                break;
            }
            loopLength--;
        }
    }
}
