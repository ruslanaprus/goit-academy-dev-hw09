package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FontHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(FontHandler.class);
    private static final String FONT_FILE_PATH = "src/main/resources/fonts/Cattie-Regular.ttf";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();

        if ("/fonts/Cattie-Regular.ttf".equals(requestPath)) {
            try {
                Path fontPath = Paths.get(FONT_FILE_PATH);
                byte[] fontBytes = Files.readAllBytes(fontPath);

                // send the font file with appropriate MIME type (font/ttf)
                sendResponse(exchange, 200, fontBytes, "font/ttf");
            } catch (IOException e) {
                logger.error("Error fetching font: {}", FONT_FILE_PATH, e);
                exchange.sendResponseHeaders(404, -1);
            }
        } else {
            logger.error("Invalid font path requested: {}", requestPath);
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, byte[] content, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }
}