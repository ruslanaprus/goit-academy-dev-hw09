package org.example.image;

import org.example.service.HttpStatusChecker;
import org.example.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImageManager {
    private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);
    private static final String IMAGE_DIR = "assets";
    private static final String DEFAULT_IMG = "src/main/resources/error/not_found.jpeg";

    private final HttpStatusChecker statusChecker;

    public ImageManager() {
        HttpUtil httpUtil = new HttpUtil(HttpClient.newHttpClient());
        this.statusChecker = new HttpStatusChecker(httpUtil);
    }

    public byte[] getImage(int statusCode) throws IOException {
        String imageFilename = statusCode + ".jpg";

        // step 1: check in the cache
        CachedImage cachedImage = CacheManager.get(imageFilename);
        if (cachedImage != null) {
            logger.info("Serving image from cache: {}", imageFilename);
            return cachedImage.getImageBytes();
        }

        // step 2: check in the filesystem
        Path imagePath = Paths.get(IMAGE_DIR, imageFilename);
        if (Files.exists(imagePath)) {
            logger.info("Serving image from filesystem: {}", imageFilename);
            byte[] imageBytes = Files.readAllBytes(imagePath);
            CacheManager.put(imageFilename, new CachedImage(imageBytes));
            return imageBytes;
        }

        // step 3: download from web
        return downloadAndCacheImage(statusCode);
    }

    private byte[] downloadAndCacheImage(int statusCode) throws IOException {
        Path tempFilePath = null;
        try {
            String imageUrl = statusChecker.getStatusImage(statusCode);
            logger.info("Downloading image from: {}", imageUrl);
            HttpStatusImageDownloader downloader = new HttpStatusImageDownloader();
            byte[] imageBytes = downloader.downloadStatusImage(imageUrl);

            // save to filesystem and cache
            Path imagePath = Paths.get(IMAGE_DIR, statusCode + ".jpg");
            Files.createDirectories(imagePath.getParent());

            // temporary file, move it to the destination only after download is complete
            tempFilePath = Files.createTempFile(imagePath.getParent(), "temp-", ".img");

            // write buffer content into temporary file
            Files.write(tempFilePath, imageBytes);

            // move temporary file to final destination
            Files.move(tempFilePath, imagePath, StandardCopyOption.REPLACE_EXISTING);

            // add bytes directly from the buffer (don't need to read the file again)
            CacheManager.put(statusCode + ".jpg", new CachedImage(imageBytes));

            return imageBytes;
        } catch (Exception e) {
            // delete temporary file if something goes wrong
            if (tempFilePath != null) {
                Files.deleteIfExists(tempFilePath);
            }
            logger.error("Failed to download image for status code: {}", statusCode, e.getMessage());
        }
        // serve the default 'not found' image in case of any error
        Path fallbackImagePath = Paths.get(DEFAULT_IMG);
        byte[] fallbackImageBytes = null;
        if (Files.exists(fallbackImagePath)) {
            fallbackImageBytes = Files.readAllBytes(fallbackImagePath);
        } else {
            logger.info("Fallback image not found");
        }
        return fallbackImageBytes;
    }
}