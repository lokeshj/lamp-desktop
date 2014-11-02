package me.lokesh.lamp.service.utils;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by lokesh on 30/10/14.
 */
public class SystemProperties {
    private static final String OS = System.getProperty("os.name").toLowerCase();

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
        return OS.startsWith("windows");
    }

    public static boolean isOSLinux() {
        return OS.startsWith("linux");
    }

    public static boolean isOSMac() {
        return OS.startsWith("mac");
    }

    public static boolean isOSAndroid() {
        return OS.startsWith("android");
    }
}
