package org.example;

import org.example.server.ImageWebServer;

import java.io.IOException;

public class AppLauncher {

    public static void main(String[] args) throws IOException {
        ImageWebServer.startServer();
    }
}