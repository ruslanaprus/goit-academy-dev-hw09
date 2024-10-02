package org.example.image;

import org.example.service.HttpStatusChecker;
import org.example.util.HttpUtil;
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

    private final HttpStatusChecker statusChecker;
    private final HttpClient client;
    private final HttpUtil httpUtil;

    public HttpStatusImageDownloader() {
        this.client = HttpClient.newHttpClient();
        this.httpUtil = new HttpUtil(client);
        this.statusChecker = new HttpStatusChecker(new HttpUtil(client));
    }

    public void downloadStatusImage(int code) {
        try {
            String imageUrl = statusChecker.getStatusImage(code);
            logger.info("Image URL: {}", imageUrl);

            String cachedImage = getCachedImage(imageUrl);
            logger.info("Image retrieved from cache.");
            if (cachedImage == null) {
                logger.info("Downloading image from the web.");
                cachedImage = downloadAndSaveImage(imageUrl);
            }

        } catch (Exception e) {
            logger.error("Failed to download image for status code {}", code, e);
        }
    }

    private String getCachedImage(String imageUrl) {
        logger.info("retreiving image from cache...");
        WeakReference<CachedImage> cachedRef = imageCache.get(imageUrl);
        logger.info("cached image: {}", cachedRef);
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
}