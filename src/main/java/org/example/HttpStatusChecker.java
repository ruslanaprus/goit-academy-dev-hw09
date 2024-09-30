package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;

public class HttpStatusChecker {
    private static final Logger logger = LoggerFactory.getLogger(HttpStatusChecker.class);
    private final HttpUtil httpUtil;
    private static final String BASE_URL = "https://http.cat/";

    public HttpStatusChecker(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
    }

    public String getStatusImage(int code) throws Exception {
        String imageUrl = BASE_URL + code + ".jpg";
        URI uri = URI.create(imageUrl);

        try {
            logger.info("Sending HEAD request to URI: {}", uri);
            HttpResponse<String> response = httpUtil.sendHeadRequest(uri);

            if (response != null && response.statusCode() == 200) {
                logger.info("Successfully retrieved image for status code: {}", code);
                return imageUrl;
            } else {
                String errorMsg = "Image not found for status code: " + code;
                logger.error(errorMsg);
                throw new Exception(errorMsg);
            }
        } catch (IOException | InterruptedException e) {
            String errorMsg = "Failed to fetch image for status code: " + code;
            logger.error(errorMsg, e);
            throw new Exception(errorMsg, e);
        }
    }
}