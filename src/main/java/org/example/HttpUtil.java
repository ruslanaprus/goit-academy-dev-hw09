package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private final HttpClient client;

    public HttpUtil(HttpClient client) {
        this.client = client;
    }

    public HttpResponse<String> sendHeadRequest(URI uri) {
        try {
            logger.info("Sending HEAD request to URI: {}", uri);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            handleError(e, uri);
        }
        return null;
    }

    private void handleError(Exception e, URI uri) {
        logger.error("Error occurred during request to URI: {}", uri, e);
        if (e instanceof InterruptedException) {
            logger.warn("InterruptedException caught, interrupting the thread");
            Thread.currentThread().interrupt();
        }
    }
}
