package org.example.image;

public class CachedImage {
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
        return System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000; // 24 hours expiration
    }
}