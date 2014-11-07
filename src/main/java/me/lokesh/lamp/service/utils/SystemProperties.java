package me.lokesh.lamp.service.utils;

import me.lokesh.lamp.service.models.Peer;
import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by lokesh.
 */
public class SystemProperties {
    private static final String OS_SYSTEM = System.getProperty("os.name").toLowerCase();
    private static String OS;

    public static String getIPAddress() {
        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                    Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                    while (inetAddressEnumeration.hasMoreElements()) {
                        InetAddress address = inetAddressEnumeration.nextElement();
                        if (InetAddressUtils.isIPv4Address(address.getHostAddress()) ) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isOSWindows() {
        return OS_SYSTEM.startsWith("windows");
    }

    public static boolean isOSLinux() {
        return OS_SYSTEM.startsWith("linux");
    }

    public static boolean isOSMac() {
        return OS_SYSTEM.startsWith("mac");
    }

    public static boolean isOSAndroid() {
        return OS_SYSTEM.startsWith("android");
    }

    public static String getOs() {
        if(OS == null) {
            if (SystemProperties.isOSWindows()) {
                OS = Peer.WINDOWS;
            } else if (SystemProperties.isOSLinux()) {
                OS = Peer.LINUX;
            } else if (SystemProperties.isOSMac()) {
                OS = Peer.MACOS;
            } else if (SystemProperties.isOSAndroid()) {
                OS = Peer.ANDROID;
            } else {
                OS = Peer.UNKNOWN;
            }
        }

        return OS;
    }
}
