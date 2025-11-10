//NioFileServer.java
package nio;

import com.google.gson.Gson;
import threading.ExceptionHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NIOFileServer {
    private final int port;
    private final int workerThreads;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private volatile boolean running = false;
    private final Gson gson = new Gson();
    // Debugging removed for production
    private static final int BUFFER_SIZE = 8192;
    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private final Map<String, FileMetadata> fileMetadataStore = new ConcurrentHashMap<>();

    public NIOFileServer(int port) {
        this(port, 4); // Default to 4 worker threads
    }

    public NIOFileServer(int port, int workerThreads) {
        this.port = port;
        this.workerThreads = workerThreads;
        try {
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to create upload directory: " + e.getMessage());
        }
    }

    public void start() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        System.out.println("[INFO] NIO File Server started on port " + port);
        System.out.println("[INFO] Using non-blocking I/O with Selector");
        System.out.println("[INFO] Upload directory: " + UPLOAD_DIR.toAbsolutePath());

        while (running) {
            try {
                int readyChannels = selector.select(1000);
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                if (running) {
//                    ExceptionHandler.handle(e, "NIO Server - selector loop");
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            System.out.println("[ACCEPT] Client connected: " + clientChannel.getRemoteAddress());
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();

        try {
            // unregister this key before switching to blocking mode to avoid IllegalBlockingModeException
            key.cancel();
            channel.configureBlocking(true);

            InputStream in = channel.socket().getInputStream();
            OutputStream out = channel.socket().getOutputStream();

            BufferedInputStream bufferedIn = new BufferedInputStream(in);

            Headers headers = readHeaders(bufferedIn);

            String requestLine = headers.getRequestLine();
            if (requestLine == null) {
                sendError(out, 400, "Bad Request");
                return;
            }

            System.out.println("[REQUEST] " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendError(out, 400, "Invalid request line");
                return;
            }

            String method = parts[0];
            String path = parts[1];

            // Handle CORS preflight
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendOptionsResponse(out, headers);
                return;
            }

            if ("POST".equals(method) && path.startsWith("/upload")) {
                handleFileUpload(bufferedIn, out, headers);
            } else if ("GET".equals(method) && path.startsWith("/download/")) {
                String fileId = path.substring("/download/".length());
                handleFileDownload(out, fileId);
            } else {
                sendError(out, 404, "Endpoint not found");
            }

        } catch (Exception e) {
//            ExceptionHandler.handle(e, "NIO Server - handling request");
        } finally {
            try {
                channel.close();
                key.cancel();
            } catch (IOException e) {
//                ExceptionHandler.handle(e, "NIO Server - closing channel");
            }
        }
    }

    private Headers readHeaders(BufferedInputStream in) throws IOException {
        Headers headers = new Headers();
        // Read headers using the BufferedInputStream directly to avoid losing
        // body bytes to a BufferedReader internal buffer. Read lines terminated by \r\n.
        String requestLine = readLine(in);
        headers.setRequestLine(requestLine);

        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.add(name, value);
            }
        }

        return headers;
    }

    private void handleFileUpload(BufferedInputStream in, OutputStream out, Headers headers) {
        try {
            String contentType = headers.get("Content-Type").orElse("");

            if (!contentType.startsWith("multipart/form-data")) {
                sendError(out, 400, "Content-Type must be multipart/form-data");
                return;
            }

            String boundary = extractBoundary(contentType);
            if (boundary == null) {
                sendError(out, 400, "Missing boundary in Content-Type");
                return;
            }

            // Processing multipart upload with boundary

            MultipartParser parser = new MultipartParser(in, boundary);
            MultipartParser.Part filePart = null;
            String taskId = "";

            while (parser.hasNextPart()) {
                MultipartParser.Part part = parser.nextPart();
                String name = part.getName();

                if ("taskId".equals(name)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = part.getInputStream().read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    taskId = baos.toString(StandardCharsets.UTF_8);
                } else if ("file".equals(name)) {
                    filePart = part;
                    break;
                }
            }

            if (filePart == null) {
                sendError(out, 400, "No file found in request");
                return;
            }

            String fileName = filePart.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = "upload_" + System.currentTimeMillis();
            }

            String fileId = "file_" + System.currentTimeMillis() + "_" +
                    Integer.toHexString(fileName.hashCode());
            Path filePath = UPLOAD_DIR.resolve(fileId + "_" + sanitizeFileName(fileName));

            long totalBytes = saveFileUsingNIO(filePart.getInputStream(), filePath);

            FileMetadata metadata = new FileMetadata(
                    fileId,
                    fileName,
                    filePath.toString(),
                    totalBytes,
                    taskId
            );
            fileMetadataStore.put(fileId, metadata);

            System.out.println("[SUCCESS] File uploaded: " + fileName + " (" + totalBytes + " bytes)");

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "fileId", fileId,
                    "fileName", fileName,
                    "size", totalBytes
            );

            sendJsonResponse(out, 200, response);

        } catch (Exception e) {
//            ExceptionHandler.handle(e, "NIO Server - file upload");
            try {
                sendError(out, 500, "Upload failed: " + e.getMessage());
            } catch (IOException ex) {
//                ExceptionHandler.handle(ex, "NIO Server - sending error response");
            }
        }
    }

    /**
     * Read a single line from the stream, terminated by CRLF (\r\n). Returns
     * the line without the CRLF. Returns null if the stream ends before any byte is read.
     */
    private static String readLine(BufferedInputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int prev = -1;
        int cur;
        while ((cur = in.read()) != -1) {
            if (prev == '\r' && cur == '\n') {
                // remove the previous CR from baos
                byte[] arr = baos.toByteArray();
                if (arr.length > 0) {
                    return new String(arr, 0, arr.length - 1, StandardCharsets.UTF_8);
                } else {
                    return "";
                }
            }
            baos.write(cur);
            prev = cur;
        }
        if (baos.size() == 0) return null;
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private long saveFileUsingNIO(InputStream inputStream, Path filePath) throws IOException {
        java.nio.channels.FileChannel fileChannel = java.nio.channels.FileChannel.open(
                filePath,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.WRITE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        );

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        byte[] byteArray = new byte[BUFFER_SIZE];
        long totalBytes = 0;
        int bytesRead;

        // writing file using FileChannel and ByteBuffer

        while ((bytesRead = inputStream.read(byteArray)) != -1) {
            buffer.clear();
            buffer.put(byteArray, 0, bytesRead);
            buffer.flip();

            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }

            totalBytes += bytesRead;
        }

        fileChannel.close();
        // file write complete: " + totalBytes + " bytes

        return totalBytes;
    }

    private void handleFileDownload(OutputStream out, String fileId) {
        try {
            FileMetadata metadata = fileMetadataStore.get(fileId);

            if (metadata == null) {
                sendError(out, 404, "File not found");
                return;
            }

            Path filePath = Paths.get(metadata.getStoredPath());

            if (!Files.exists(filePath)) {
                sendError(out, 404, "File not found on disk");
                return;
            }

            System.out.println("[DOWNLOAD] Serving file: " + metadata.getOriginalName());

            java.nio.channels.FileChannel fileChannel = java.nio.channels.FileChannel.open(
                    filePath,
                    java.nio.file.StandardOpenOption.READ
            );

            long fileSize = fileChannel.size();

            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Disposition: attachment; filename=\"" + metadata.getOriginalName() + "\"\r\n" +
                    "Content-Length: " + fileSize + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "\r\n";
            out.write(headers.getBytes(StandardCharsets.UTF_8));
            out.flush();

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            long position = 0;

            System.out.println("[NIO] Reading file using FileChannel and ByteBuffer...");

            while (position < fileSize) {
                buffer.clear();
                int bytesRead = fileChannel.read(buffer);

                if (bytesRead == -1) break;

                buffer.flip();

                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                out.write(bytes);

                position += bytesRead;
            }

            out.flush();
            fileChannel.close();

            System.out.println("[SUCCESS] File download complete: " + fileSize + " bytes sent");

        } catch (Exception e) {
//            ExceptionHandler.handle(e, "NIO Server - file download");
            try {
                sendError(out, 500, "Download failed");
            } catch (IOException ex) {
//                ExceptionHandler.handle(ex, "NIO Server - sending error");
            }
        }
    }

    private void sendJsonResponse(OutputStream out, int statusCode, Map<String, Object> data) throws IOException {
        String body = gson.toJson(data);
        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "\r\n" +
                body;
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void sendOptionsResponse(OutputStream out, Headers requestHeaders) throws IOException {
        String allowMethods = requestHeaders.get("Access-Control-Request-Method").orElse("POST, GET, OPTIONS");
        String allowHeaders = requestHeaders.get("Access-Control-Request-Headers").orElse("Content-Type, X-Requested-With, Accept");

        String response = "HTTP/1.1 200 OK\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: " + allowMethods + "\r\n" +
                "Access-Control-Allow-Headers: " + allowHeaders + "\r\n" +
                "Access-Control-Max-Age: 86400\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void sendError(OutputStream out, int statusCode, String message) throws IOException {
        Map<String, Object> error = Map.of(
                "status", "error",
                "message", message
        );
        sendJsonResponse(out, statusCode, error);
    }

    private String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                String boundary = part.substring("boundary=".length());
                if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                return boundary;
            }
        }
        return null;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public void stop() {
        running = false;
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
            System.out.println("[INFO] NIO File Server stopped");
        } catch (IOException e) {
//            ExceptionHandler.handle(e, "NIO Server - shutdown");
        }
    }

    private static class Headers {
        private String requestLine;
        private final Map<String, String> headers = new ConcurrentHashMap<>();

        public void setRequestLine(String line) {
            this.requestLine = line;
        }

        public String getRequestLine() {
            return requestLine;
        }

        public void add(String name, String value) {
            headers.put(name, value);
        }

        public java.util.Optional<String> get(String name) {
            return java.util.Optional.ofNullable(headers.get(name));
        }
    }

    private static class MultipartParser {
        private final BufferedInputStream in;
        private final byte[] boundary;

        public MultipartParser(BufferedInputStream in, String boundary) {
            this.in = in;
            this.boundary = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
            // MultipartParser initialized with boundary
        }

        public boolean hasNextPart() throws IOException {
            in.mark(boundary.length + 4);
            byte[] buffer = new byte[boundary.length];
            int read = in.read(buffer);
            in.reset();
            boolean has = read > 0;
            // multipart parser peek
            return has;
        }

        public Part nextPart() throws IOException {
            skipToBoundary();

            // consume the rest of the boundary line (CRLF)
            readLine(in);
            // read part headers (until empty line)
            Map<String, String> partHeaders = new HashMap<>();
            String line;
            while ((line = readLine(in)) != null && !line.isEmpty()) {
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String name = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    partHeaders.put(name, value);
                }
            }

            // multipart part headers logged

            // Return a stream that stops at the next boundary (so callers can read the part body safely)
            return new Part(partHeaders, new PartInputStream(in, boundary));
        }

        /** InputStream wrapper that ends when the next boundary is encountered. */
        private static class PartInputStream extends InputStream {
            private final BufferedInputStream in;
            private final byte[] boundary;
            private boolean eof = false;

            public PartInputStream(BufferedInputStream in, byte[] boundary) {
                this.in = in;
                this.boundary = boundary;
            }

            @Override
            public int read() throws IOException {
                if (eof) return -1;

                in.mark(boundary.length + 4);
                byte[] peek = new byte[boundary.length];
                int r = in.read(peek);
                if (r == -1) {
                    in.reset();
                    return -1;
                }

                boolean match = r == boundary.length;
                if (match) {
                    for (int i = 0; i < boundary.length; i++) {
                        if (peek[i] != boundary[i]) { match = false; break; }
                    }
                } else {
                    match = false;
                }

                if (match) {
                    // boundary ahead -> do not consume it here; reset and signal EOF
                    in.reset();
                    eof = true;
                    return -1;
                }

                // not a boundary, reset and read single byte normally
                in.reset();
                int b = in.read();
                return b;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (eof) return -1;
                int c = read();
                if (c == -1) return -1;
                b[off] = (byte) c;
                int read = 1;
                for (; read < len; read++) {
                    int r = read();
                    if (r == -1) break;
                    b[off + read] = (byte) r;
                }
                return read;
            }
        }

        private void skipToBoundary() throws IOException {
            byte[] buffer = new byte[1];
            int matchIndex = 0;

            while (in.read(buffer) != -1) {
                if (buffer[0] == boundary[matchIndex]) {
                    matchIndex++;
                    if (matchIndex == boundary.length) {
                        return;
                    }
                } else {
                    matchIndex = 0;
                }
            }
        }

        public static class Part {
            private final Map<String, String> headers;
            private final InputStream inputStream;

            public Part(Map<String, String> headers, InputStream inputStream) {
                this.headers = headers;
                this.inputStream = inputStream;
            }

            public String getName() {
                String disposition = headers.get("Content-Disposition");
                if (disposition != null) {
                    for (String part : disposition.split(";")) {
                        part = part.trim();
                        if (part.startsWith("name=")) {
                            return stripQuotes(part.substring(5));
                        }
                    }
                }
                return null;
            }

            public String getFileName() {
                String disposition = headers.get("Content-Disposition");
                if (disposition != null) {
                    for (String part : disposition.split(";")) {
                        part = part.trim();
                        if (part.startsWith("filename=")) {
                            return stripQuotes(part.substring(9));
                        }
                    }
                }
                return null;
            }

            public InputStream getInputStream() {
                return inputStream;
            }

            private String stripQuotes(String str) {
                if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
                    return str.substring(1, str.length() - 1);
                }
                return str;
            }
        }

        private static class HashMap<K, V> extends java.util.HashMap<K, V> {}
    }

    private static class FileMetadata {
        private final String fileId;
        private final String originalName;
        private final String storedPath;
        private final long size;
        private final String taskId;

        public FileMetadata(String fileId, String originalName, String storedPath, long size, String taskId) {
            this.fileId = fileId;
            this.originalName = originalName;
            this.storedPath = storedPath;
            this.size = size;
            this.taskId = taskId;
        }

        public String getFileId() { return fileId; }
        public String getOriginalName() { return originalName; }
        public String getStoredPath() { return storedPath; }
        public long getSize() { return size; }
        public String getTaskId() { return taskId; }
    }

    public static void main(String[] args) throws IOException {
        NIOFileServer server = new NIOFileServer(8081);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[INFO] Shutting down NIO server...");
            server.stop();
        }));

        server.start();
    }
}