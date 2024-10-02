package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AssetHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(AssetHandler.class);
    private final String baseDirectory;

    public AssetHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filePath = baseDirectory + exchange.getRequestURI().getPath().replace("/images", "");
        File file = new File(filePath);
        if(file.exists() && !file.isDirectory()) {
            exchange.sendResponseHeaders(200, file.length());
            try(OutputStream os = exchange.getResponseBody();
                FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }
}