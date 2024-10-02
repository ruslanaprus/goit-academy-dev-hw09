package org.example.server;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageWebServer {
    private static final Logger logger = LoggerFactory.getLogger(ImageWebServer.class);
    public static final int MAX_CONNECTIONS = 100;
    public static final int DEFAULT_PORT = 8080;
    public static final String IMAGE_DIR = "assets";

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        server.createContext("/", new StatusHandler());
        server.createContext("/images", new AssetHandler(IMAGE_DIR));
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        server.setExecutor(executor);
        server.start();
        logger.info("Server started at http://localhost:{}", DEFAULT_PORT);
    }
}