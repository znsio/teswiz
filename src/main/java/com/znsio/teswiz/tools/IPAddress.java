package com.znsio.teswiz.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class IPAddress {
    public static String getPublicIPAddress() {
        try {
            // Make a request to a service that echoes back the public IP address
            URL url = new URL("https://api.ipify.org");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String publicIp = reader.readLine();
            System.out.println("Public IP Address: " + publicIp);
            return publicIp;
        } catch (IOException e) {
            throw new RuntimeException("Error getting public IP address: " + e.getMessage(), e);
        }
    }

    public static String getPrivateIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String localHostAddress = localHost.getHostAddress();
            System.out.println("Local IP Address: " + localHostAddress);
            return localHostAddress;
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to determine localhost IP address: " + e.getMessage(), e);
        }
    }
}
