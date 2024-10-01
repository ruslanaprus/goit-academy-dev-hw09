package org.example;

import java.io.IOException;

public class AppLauncher {

    public static void main(String[] args) throws IOException {
        HttpStatusImageDownloader downloader = new HttpStatusImageDownloader();
        downloader.downloadStatusImage(200);

        ImageWebServer.startServer();
    }
}