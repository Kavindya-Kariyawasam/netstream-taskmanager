package nio;

import com.google.gson.Gson;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileUploadHandler {
    private static final long MAX_FILE_BYTES = 50L * 1024L * 1024L;
    private static final Path STORAGE_DIR = Paths.get("backend", "uploads");
    private static final Map<String, FileMetadata> METADATA_STORE = new ConcurrentHashMap<>();

    private final BufferedInputStream in;
    private final OutputStream out;
    private final Headers headers;
    private final Gson gson = new Gson();

    public FileUploadHandler(BufferedInputStream in, OutputStream out, Headers headers) {
        this.in = in;
        this.out = out;
        this.headers = headers;
        try {
            Files.createDirectories(STORAGE_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create directory: " + e.getMessage(), e);
        }
    }

    public String handle() throws IOException {
        Optional<String> ctOpt = headers.get("content-type");
        if (ctOpt.isEmpty() || !ctOpt.get().startsWith("multipart/form-data")) {
            return null; // let router handle error response
        }

        String contentType = ctOpt.get();
        String boundary = extractBoundary(contentType);
        if (boundary == null) return null;

        MultipartStream multipart = new MultipartStream(in, boundary.getBytes(StandardCharsets.UTF_8), MAX_FILE_BYTES);
        Map<String, String> formFields = new HashMap<>();
        String fileId = null;
        String originalFileName = null;
        Path storedFile = null;

        while (multipart.hasNextPart()) {
            MultipartStream.Part part = multipart.nextPart();
            if (part == null) break;

            String disposition = part.getHeaders().getOrDefault("content-disposition", "");
            String name = parseName(disposition);
            if (name == null) continue;

            if ("file".equals(name)) {
                originalFileName = parseFileName(disposition);
                if (originalFileName == null) originalFileName = "upload.bin";

                String safeName = sanitizeFileName(originalFileName);
                fileId = "file_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
                storedFile = STORAGE_DIR.resolve(fileId + "_" + safeName);

                try (FileChannel fileChannel = FileChannel.open(storedFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                     InputStream pis = part.getBodyStream()) {

                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = pis.read(buffer)) != -1) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);
                        while (byteBuffer.hasRemaining()) fileChannel.write(byteBuffer);
                    }
                }

                FileMetadata meta = new FileMetadata(fileId, originalFileName, storedFile.toString(), Files.size(storedFile),
                        formFields.get("taskId"), formFields.get("description"));
                METADATA_STORE.put(fileId, meta);
            } else {
                formFields.put(name, new String(part.readAllBytes(), StandardCharsets.UTF_8));
            }
        }

        return storedFile != null ? storedFile.getFileName().toString() : null;
    }

    // ---------------------- Helper Methods --------------------------

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String parseName(String disposition) {
        for (String part : disposition.split(";")) {
            part = part.trim();
            if (part.startsWith("name=")) {
                return stripQuotes(part.substring(5).trim());
            }
        }
        return null;
    }

    private static String parseFileName(String disposition) {
        for (String part : disposition.split(";")) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                return stripQuotes(part.substring(9).trim());
            }
        }
        return null;
    }

    private static String stripQuotes(String str) {
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static String extractBoundary(String contentType) {
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

    private void writeJsonResponse(int statusCode, Map<String, Object> map) throws IOException {
        String body = gson.toJson(map);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.write("HTTP/1.1 " + statusCode + " OK\r\n");
        writer.write("Content-Type: application/json\r\n");
        writer.write("Access-Control-Allow-Origin: *\r\n");
        writer.write("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n");
        writer.write("Access-Control-Allow-Headers: Content-Type\r\n");
        writer.write("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
        writer.write("\r\n");
        writer.write(body);
        writer.flush();
    }

    public static Optional<FileMetadata> getMetadata(String fileId) {
        return Optional.ofNullable(METADATA_STORE.get(fileId));
    }
}
