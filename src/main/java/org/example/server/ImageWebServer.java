package org.example.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.image.HttpStatusImageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageWebServer {
    private static final Logger logger = LoggerFactory.getLogger(ImageWebServer.class);
    public static final int MAX_CONNECTIONS = 100;

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new StatusHandler());
        server.createContext("/images", new AssetHandler("assets"));
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        server.setExecutor(executor);
        server.start();
        logger.info("Server started at http://localhost:8080");
    }

    static class AssetHandler implements HttpHandler {
        private final String baseDirectory;

        AssetHandler(String baseDirectory) {
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

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpStatusImageDownloader downloader = new HttpStatusImageDownloader();
            String path = exchange.getRequestURI().getPath();
            String statusCode = path.substring(path.lastIndexOf('/') + 1);
            downloader.downloadStatusImage(Integer.parseInt(statusCode));

            String response = """
                    <!DOCTYPE html>
                         <html lang="en">
                         <head>
                             <meta charset="UTF-8">
                             <meta http-equiv="X-UA-Compatible" content="IE=edge">
                             <meta name="viewport" content="width=device-width, initial-scale=1.0">
                             <title>Status Image</title>
                             <style>
                                 body {
                                     font-family: Arial, sans-serif;
                                     text-align: center;
                                     padding-top: 50px;
                                 }
                                 img {
                                     max-width: 100%;
                                     height: auto;
                                     border: 1px solid #ddd;
                                     border-radius: 4px;
                                     padding: 5px;
                                     width: 300px;
                                 }
                                 h1 {
                                     color: #333;
                                 }
                             </style>
                         </head>
                         <body>
                         <h1>HTTP Status Image</h1>
                         <p>The image below shows the HTTP status code you requested:</p>
                    
                         <!-- Display the image -->
                         <img src="./images/""" + statusCode +
                    """
                            .jpg" alt=""" + statusCode +
                    """
                            >
                            <p>Image fetched from <a href="https://http.cat">https://http.cat</a>.</p>
                            </body>
                            </html>
                            """;

            byte[] responseBytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(responseBytes.length));
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}