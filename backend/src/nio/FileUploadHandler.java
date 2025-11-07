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

    public void handle() throws IOException {
        Optional<String> ctOpt = headers.get("content-type");
        if (ctOpt.isEmpty() || !ctOpt.get().startsWith("multipart/form-data")) {
            writeJsonResponse(400, Map.of("status", "error", "message", "Content-Type must be multipart/form-data"));
            return;
        }

        String contentType = ctOpt.get();
        String boundary = extractBoundary(contentType);
        System.out.println("➡️ Extracted boundary: " + boundary);

        if (boundary == null) {
            writeJsonResponse(400, Map.of("status", "error", "message", "Missing boundary"));
            return;
        }

        MultipartStream multipart = new MultipartStream(in, boundary.getBytes(StandardCharsets.UTF_8), MAX_FILE_BYTES);
        Map<String, String> formFields = new HashMap<>();
        String fileId = null;
        String originalFileName = null;
        Path storedFile = null;
        long totalBytes = 0;

        System.out.println("🚀 Starting to read multipart parts...");

        while (multipart.hasNextPart()) {
            MultipartStream.Part part = multipart.nextPart();
            System.out.println("🧾 Got a new part: " + part.getHeaders());

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
                    long bytesWritten = 0;

                    while ((read = pis.read(buffer)) != -1) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);
                        while (byteBuffer.hasRemaining()) fileChannel.write(byteBuffer);
                        bytesWritten += read;

                        if (bytesWritten % (1024 * 10) == 0) { // every 10KB
                            System.out.println("📦 Written " + bytesWritten + " bytes so far...");
                        }

                        if (bytesWritten > MAX_FILE_BYTES) {
                            try { Files.deleteIfExists(storedFile); } catch (IOException ignored) {}
                            writeJsonResponse(413, Map.of("status", "error", "message", "File too large (Max 50MB)"));
                            return;
                        }
                    }

// 🔚 Reached end of file part
                    System.out.println("✅ Finished writing file: " + storedFile + " (" + bytesWritten + " bytes)");


                    totalBytes += bytesWritten;
                    System.out.println("✅ File saved successfully: " + storedFile + " (" + bytesWritten + " bytes)");
                }

                FileMetadata meta = new FileMetadata(fileId, originalFileName, storedFile.toString(), totalBytes,
                        formFields.get("taskId"), formFields.get("description"));
                METADATA_STORE.put(fileId, meta);

            } else {
                // Read text fields
                String value = new String(part.readAllBytes(), StandardCharsets.UTF_8);
                formFields.put(name, value);
            }
        }

        if (fileId == null) {
            writeJsonResponse(400, Map.of("status", "error", "message", "No file part 'file' found"));
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("fileId", fileId);
        response.put("fileName", originalFileName);
        response.put("size", totalBytes);

        writeJsonResponse(200, response);
        System.out.println("🎯 Upload completed, response sent to client ✅");
    }

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
        writer.write("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
        writer.write("\r\n");
        writer.write(body);
        writer.flush();
    }

    public static Optional<FileMetadata> getMetadata(String fileId) {
        return Optional.ofNullable(METADATA_STORE.get(fileId));
    }
}
