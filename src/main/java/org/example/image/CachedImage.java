package org.example.image;

public class CachedImage {
    private final byte[] imageBytes;
    private final long timestamp;

    public CachedImage(byte[] imageBytes) {
        this.imageBytes = imageBytes;
        this.timestamp = System.currentTimeMillis();
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000; // 24 hours expiration
    }
}