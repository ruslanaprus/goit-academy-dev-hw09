package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import org.example.image.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class AssetHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(AssetHandler.class);
    private final ImageManager imageManager;

    public AssetHandler() {
        this.imageManager = new ImageManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filePath = exchange.getRequestURI().getPath().replace("/images/", "");
        try {
            int statusCode = Integer.parseInt(filePath.replace(".jpg", ""));
            byte[] imageBytes = imageManager.getImage(statusCode);

            sendResponse(exchange, 200, imageBytes);
        } catch (NumberFormatException | IOException e) {
            logger.error("Error fetching image: {}", filePath, e);
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, byte[] content) throws IOException {
        exchange.sendResponseHeaders(statusCode, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

}