package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Map;
import java.util.WeakHashMap;

public class HttpStatusImageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(HttpStatusImageDownloader.class);
    private static final String IMAGE_DIR = "assets/";
    private static final Map<String, WeakReference<CachedImage>> imageCache = new WeakHashMap<>();

    public void downloadStatusImage(int code) {
        HttpClient client = HttpClient.newHttpClient();
        HttpUtil httpUtil = new HttpUtil(client);
        HttpStatusChecker statusChecker = new HttpStatusChecker(httpUtil);

        try {
            String imageUrl = statusChecker.getStatusImage(code);
            logger.info("Image URL: {}", imageUrl);

            String encodedImage = getCachedImage(imageUrl);
            if (encodedImage != null) {
                logger.info("Image retrieved from cache.");
            } else {
                logger.info("Downloading image from the web.");
                encodedImage = downloadAndSaveImage(imageUrl);
            }
            logger.info("Base64 Image: {}", encodedImage);

        } catch (Exception e) {
            logger.error("Failed to download image for status code {}", code, e);
        }
    }

    private String getCachedImage(String imageUrl) {
        WeakReference<CachedImage> cachedRef = imageCache.get(imageUrl);
        if (cachedRef != null) {
            CachedImage cachedImage = cachedRef.get();
            if (cachedImage != null && !cachedImage.isExpired()) {
                return cachedImage.getBase64Image();
            }
        }
        return null;
    }

    private String downloadAndSaveImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path imagePath = Paths.get(IMAGE_DIR + fileName);

        Files.createDirectories(imagePath.getParent());

        try (InputStream in = url.openStream()) {
            Files.copy(in, imagePath, StandardCopyOption.REPLACE_EXISTING);
        }

        byte[] imageBytes = Files.readAllBytes(imagePath);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        imageCache.put(imageUrl, new WeakReference<>(new CachedImage(base64Image)));

        return base64Image;
    }

    private static class CachedImage {
        private final String base64Image;
        private final long timestamp;

        public CachedImage(String base64Image) {
            this.base64Image = base64Image;
            this.timestamp = System.currentTimeMillis();
        }

        public String getBase64Image() {
            return base64Image;
        }

        public boolean isExpired() {
            // Check if the cached image is older than 24 hours
            return System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000;
        }
    }
}
