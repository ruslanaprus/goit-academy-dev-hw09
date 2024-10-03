# Status Code Cats Web Server

The `ImageWebServer` is a HTTP server that serves images representing different HTTP status codes. These images are fetched from the `https://http.cat` API or served from a local cache. The server also supports serving static assets such as fonts and handles caching mechanisms to optimize the performance of repeated requests.

## Project Structure

The application is built using a custom HTTP server based on `com.sun.net.httpserver.HttpServer`. This server is efficient enough for the task and can handle multiple connections with its multi-threaded design using a thread pool of 100 connections. It incorporates the following key components:

1. **HttpServer with Context Handlers**:

    - The server routes incoming requests to specific endpoints using context handlers (`StatusHandler`, `AssetHandler`, `FontHandler`).
2. **Separation of Concerns**:

    - `StatusHandler`: Handles requests for HTTP status code images.
    - `AssetHandler`: Serves image files from a local directory.
    - `FontHandler`: Serves static font files.
3. **Caching and Optimization**:

    - Caching is implemented at two levels:
        - **In-Memory Cache**: Stores recently fetched images to minimize file system access and API calls.
        - **File System Cache**: Stores images in a local directory to avoid repeated downloads from the remote API.
    - **Image Expiration**: Cached images are valid for 24 hours.
4. **Thread Pooling**:

    - The server uses a fixed thread pool (`MAX_CONNECTIONS = 100`) to ensure that multiple requests can be processed concurrently.

## Communication

### 1. Between `ImageWebServer` and `http.cat` API

- When a request for an image representing a particular HTTP status code (e.g., `200`) is received:
    - **Step 1**: The `StatusHandler` checks if the requested image is available in memory or on the local file system.
    - **Step 2**: If not found, the server makes a **HEAD request** to the `https://http.cat` API to verify the image’s existence.
    - **Step 3**: If the image exists, it is downloaded, stored locally, and cached in memory for future use.

### 2. Between `ImageWebServer` and Client (Browser)

- A user requests a specific HTTP status image by navigating to the URL in the format `http://localhost:8080/[status_code]`.
    - **StatusHandler** responds with an HTML page displaying the requested image.
- For assets like images (`/images/[status_code].jpg`) or fonts (`/fonts/Cattie-Regular.ttf`):
    - The appropriate handler (`AssetHandler`, `FontHandler`) serves the requested resource.

### 3. Between `ImageWebServer` and File System

- If an image for a requested status code is available on the local file system, it is retrieved from the `assets` directory and served to the client.
- If the image is not found, it is downloaded from the `http.cat` API, saved in the `assets` directory, and stored in memory.

## Caching Mechanism & Image Handling Optimization

### Caching Strategy:

1. **In-Memory Caching**:

    - Recently requested images are cached in-memory using the `CacheManager`. Images are stored as `CachedImage` objects, which include the image bytes and a timestamp.
    - **Expiration**: Images expire after 24 hours, after which they are evicted from memory.
2. **File System Caching**:

    - Images are also cached on the file system in the `assets/` directory. Before fetching an image from the remote API, the file system is checked for a locally stored copy.
    - **Failover**: If downloading an image from the `http.cat` API fails, a fallback image (`not_found.jpeg`) is served.

### Image Handling Optimization:

- **HEAD Requests**: Before downloading images, the application sends a lightweight **HEAD** request to ensure the image exists at the source.
- **Temporary Files**: During the download, images are written to temporary files to avoid partial downloads in case of network errors.
- **Concurrency**: Multiple requests are handled concurrently through a thread pool, allowing efficient image processing and serving.

## Usage Instructions

### Prerequisites

- Java 11 or higher
- Internet connection (for fetching images from `https://http.cat`)

### Running the Server

- **Clone the Repository**:

```shell
git clone git@github.com:ruslanaprus/goit-academy-dev-hw09.git
cd goit-academy-dev-hw09
```

- **Build the Project**: You can build the project using Gradle

```shell
./gradlew clean build
```

- **Start the Server**: Run the main class to launch the server.

```shell
gradle run
```

  Alternatively, you can run the application directly from the IDE by executing the `AppLauncher` class.

- **Access the Server**: The server runs by default on `http://localhost:8080`. Open your browser and access the following endpoints:

**Home Page**:
  ```shell
http://localhost:8080/
  ```

Displays a welcome page with instructions.

**Request an HTTP Status Code Image**:
  ```shell
http://localhost:8080/[status_code]
  ```
Example: `http://localhost:8080/200` to request a 200 image.
   
**Access Image Directly**:

```shell
http://localhost:8080/images/[status_code].jpg
```
Example: `http://localhost:8080/images/200.jpg`