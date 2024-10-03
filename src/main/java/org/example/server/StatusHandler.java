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

        if (path.equals("/") || path.equals("/index.html")) {
            String response = generateDefaultHtmlResponse("Use /[status_code] to fetch an image.");
            sendHtmlResponse(exchange, response);
            return;
        }

        String statusCode = path.substring(path.lastIndexOf('/') + 1);

        try {
            int code = Integer.parseInt(statusCode);
            imageManager.getImage(code);
            String response = generateHtmlResponse(statusCode);
            sendHtmlResponse(exchange, response);
        } catch (NumberFormatException e) {
            logger.error("Invalid status code: {}", statusCode, e.getMessage());
            String response = generateDefaultHtmlResponse("Invalid status code: " + statusCode + ". Please enter a valid number");
            sendHtmlResponse(exchange, response);
        }
    }

    private static void sendHtmlResponse(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(responseBytes.length));
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String generateDefaultHtmlResponse(String message) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="X-UA-Compatible" content="IE=edge">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Status Image</title>
                    <style>
                        @font-face {
                                font-family: 'CustomFont';
                                src: url('src/main/resources/fonts/Cattie-Regular.ttf') format('truetype');
                            }
                        body {
                            font-family: 'CustomFont', Arial, sans-serif;
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
                    <p>""" + message +
                """
                        </p>
                        </body>
                        </html>
                        """;
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
                            @font-face {
                                font-family: 'CustomFont';
                                src: url('src/main/resources/fonts/Cattie-Regular.ttf') format('truetype');
                            }
                            body {
                                font-family: 'CustomFont', Arial, sans-serif;
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
                        <p>Images are fetched from <a href="https://http.cat">https://http.cat</a>.</p>
                        </body>
                        </html>
                        """;
    }
}