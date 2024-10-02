package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import org.example.image.CachedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssetHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(AssetHandler.class);
    private final String baseDirectory;
    private static final Map<String, CachedImage> imageCache = new ConcurrentHashMap<>();

    public AssetHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filePath = baseDirectory + exchange.getRequestURI().getPath().replace("/images", "");
        CachedImage cachedImage = imageCache.get(filePath);

        if (cachedImage != null && !cachedImage.isExpired()) {
            logger.info("Serving image from cache: {}", filePath);
            byte[] imageBytes = cachedImage.getImageBytes();
            sendResponse(exchange, 200, imageBytes);
        } else {
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                logger.info("File found on disk: {}. Caching and sending response.", filePath);
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                imageCache.put(filePath, new CachedImage(fileBytes));
                sendResponse(exchange, 200, fileBytes);
            } else {
                logger.warn("File not found: {}. Sending 404.", filePath);
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, byte[] content) throws IOException {
        exchange.sendResponseHeaders(statusCode, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

}