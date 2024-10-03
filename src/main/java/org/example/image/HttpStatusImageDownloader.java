package org.example.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class HttpStatusImageDownloader {

    public byte[] downloadStatusImage(String imageUrl) throws IOException {
        URI uri = URI.create(imageUrl);
        URL url = uri.toURL();
        try (InputStream in = url.openStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
}