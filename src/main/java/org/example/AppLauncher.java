package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;

public class AppLauncher {

    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        HttpUtil httpUtil = new HttpUtil(client);

        HttpStatusChecker statusChecker = new HttpStatusChecker(httpUtil);

        // Test the getStatusImage method with status code 200
        try {
            logger.info("Attempting to fetch image for status code 200");
            String imageUrl = statusChecker.getStatusImage(302);
            System.out.println("Image URL: " + imageUrl);
        } catch (Exception e) {
            logger.error("Failed to fetch image for status code 200", e);
        }

        // Testing with a non-existing status code 10000
        try {
            logger.info("Attempting to fetch image for non-existing status code 10000");
            String imageUrl = statusChecker.getStatusImage(10000);
            System.out.println("Image URL: " + imageUrl);
        } catch (Exception e) {
            logger.error("Failed to fetch image for non-existing status code 10000", e);
        }
    }
}