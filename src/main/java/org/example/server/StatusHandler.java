package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import org.example.image.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class StatusHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusHandler.class);
    private final ImageManager imageManager;

    public StatusHandler() {
        this.imageManager = new ImageManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String statusCode = path.substring(path.lastIndexOf('/') + 1);

        try {
            int code = Integer.parseInt(statusCode);
            imageManager.getImage(code);
            String response = generateHtmlResponse(statusCode);

            byte[] responseBytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(responseBytes.length));
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid status code: {}", statusCode, e);
            exchange.sendResponseHeaders(400, -1);
        }
    }

    private String generateHtmlResponse(String statusCode) {
        return """
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
    }
}