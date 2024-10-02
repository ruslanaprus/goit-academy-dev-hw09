package org.example.image;

import org.example.service.HttpStatusChecker;
import org.example.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpStatusImageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(HttpStatusImageDownloader.class);
    private static final String IMAGE_DIR = "assets";
    private static final Map<String, CachedImage> imageCache = new ConcurrentHashMap<>();

    private final HttpStatusChecker statusChecker;
    private final HttpUtil httpUtil;

    public HttpStatusImageDownloader() {
        this.httpUtil = new HttpUtil(HttpClient.newHttpClient());
        this.statusChecker = new HttpStatusChecker(httpUtil);
    }

    public void downloadStatusImage(int code) {
        try {
            String imageUrl = statusChecker.getStatusImage(code);
            logger.info("Image URL: {}", imageUrl);

            byte[] cachedImage = getCachedImage(imageUrl);
            if (cachedImage == null) {
                logger.info("Downloading image from the web.");
                cachedImage = downloadAndSaveImage(imageUrl);
            }
        } catch (Exception e) {
            logger.error("Failed to download image for status code {}", code, e);
        }
    }

    private byte[] getCachedImage(String imageUrl) {
        logger.info("retreiving image from cache...");
        CachedImage cachedImage = imageCache.get(imageUrl);
        logger.info("cached image: {}", cachedImage);
        if (cachedImage != null) {
            if (!cachedImage.isExpired()) {
                logger.info("Cached image found for URL: {}", imageUrl);
                return cachedImage.getImageBytes();
            } else {
                logger.info("Cached image expired for URL: {}", imageUrl);
                imageCache.remove(imageUrl);
            }
        }
        return null;
    }

    private byte[] downloadAndSaveImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path imagePath = Paths.get(IMAGE_DIR).resolve(fileName);

        Files.createDirectories(imagePath.getParent());

        // temporary file, move it to the destination only after download is complete
        Path tempFilePath = Files.createTempFile(imagePath.getParent(), "temp-", ".img");

        try (InputStream in = url.openStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            // write buffer content into temporary file
            Files.write(tempFilePath, buffer.toByteArray());

            // move temporary file to final destination
            Files.move(tempFilePath, imagePath, StandardCopyOption.REPLACE_EXISTING);

            // add bytes to array directly from the buffer (don't need to read the file again)
            byte[] imageBytes = buffer.toByteArray();

            // cache the image
            imageCache.put(imageUrl, new CachedImage(imageBytes));  // Cache binary data

            return imageBytes;
        } catch (IOException e) {
            // delete temporary file if something goes wrong
            Files.deleteIfExists(tempFilePath);
            throw e;
        }
    }
}