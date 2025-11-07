package nio;

import com.google.gson.Gson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;

public class FileDownloadHandler {
    private final OutputStream outputStream;
    private final String fieldId;
    private final Headers headers;
    private final Gson gson = new Gson();

    private static final Path STORAGE_DIR = Paths.get("backend", "uploads");

    public FileDownloadHandler(OutputStream outputStream, String fieldId, Headers headers) {
        this.outputStream = outputStream;
        this.fieldId = fieldId;
        this.headers = headers;
    }

    public void handle() throws IOException {
        // ✅ 1. Check if the file exists in the metadata store
        Optional<FileMetadata> metaOpt = FileUploadHandler.getMetadata(fieldId);

        Path path;
        String originalName;

        if (metaOpt.isPresent()) {
            FileMetadata meta = metaOpt.get();
            path = Paths.get(meta.getStoredPath()).toAbsolutePath();
            originalName = meta.getOriginalName();
            System.out.println("🔍 Found metadata for fileId: " + fieldId);
            System.out.println("📂 Stored path: " + path);
        } else {
            // ✅ 2. If not found in metadata, check the storage folder directly
            path = STORAGE_DIR.resolve(fieldId).toAbsolutePath();
            originalName = fieldId.substring(fieldId.indexOf('_') + 1);
            System.out.println("⚠️ Metadata not found. Trying direct path: " + path);
        }

        // ✅ 3. Check if file exists
        if (!Files.exists(path)) {
            System.out.println("❌ File missing: " + path);
            writeJsonResponse(404, "error", "File missing on server");
            return;
        }

        // ✅ 4. Send file response
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long size = fileChannel.size();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Disposition: attachment; filename=\"" + originalName + "\"\r\n" +
                    "Content-Length: " + size + "\r\n" +
                    "\r\n";

            bufferedOutputStream.write(header.getBytes(StandardCharsets.UTF_8));
            bufferedOutputStream.flush();

            ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
            long position = 0;

            while (position < size) {
                buffer.clear();
                int read = fileChannel.read(buffer);
                if (read == -1) break;
                buffer.flip();

                byte[] tmp = new byte[buffer.remaining()];
                buffer.get(tmp);
                bufferedOutputStream.write(tmp);
                position += read;
            }

            bufferedOutputStream.flush();
            System.out.println("✅ File successfully sent: " + originalName + " (" + size + " bytes)");
        }
    }

    private void writeJsonResponse(int statusCode, String status, String message) throws IOException {
        String body = gson.toJson(new Simple(status, message));
        BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        );
        bufferedWriter.write("HTTP/1.1 " + statusCode + "\r\n");
        bufferedWriter.write("Content-Type: application/json\r\n");
        bufferedWriter.write("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write(body);
        bufferedWriter.flush();
    }

    static class Simple {
        String status;
        String message;

        Simple(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
