package org.example.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private static final Map<String, CachedImage> cache = new ConcurrentHashMap<>();

    public static void put(String key, CachedImage image) {
        cache.put(key, image);
        logger.info("Cached image for key: {}", key);
    }

    public static CachedImage get(String key) {
        CachedImage cachedImage = cache.get(key);
        if (cachedImage != null && !cachedImage.isExpired()) {
            logger.info("Returning cached image for key: {}", key);
            return cachedImage;
        } else {
            if (cachedImage != null) {
                logger.info("Cached image expired for key: {}", key);
                cache.remove(key);
            }
            return null;
        }
    }
}