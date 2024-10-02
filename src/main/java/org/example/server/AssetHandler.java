package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import org.example.image.CacheManager;
import org.example.image.CachedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AssetHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(AssetHandler.class);
    private final String baseDirectory;

    public AssetHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filePath = baseDirectory + exchange.getRequestURI().getPath().replace("/images", "");

        // extract the filename from the filePath to use as the cache key
        String fileName = extractFilenameFromPath(filePath);

        CachedImage cachedImage = CacheManager.get(fileName);

        if (cachedImage != null) {
            logger.info("Serving image from cache: {}", fileName);
            sendResponse(exchange, 200, cachedImage.getImageBytes());
        } else {
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                logger.info("File found on disk: {}. Caching and sending response.", filePath);
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                CacheManager.put(fileName, new CachedImage(fileBytes));
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

    private String extractFilenameFromPath(String filePath) {
        Path path = Path.of(filePath);
        return path.getFileName().toString();
    }

}