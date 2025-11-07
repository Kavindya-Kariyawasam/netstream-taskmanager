package nio;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class MultipartStream {

    private final InputStream in;
    private final byte[] boundary;
    private final long maxSize;
    private boolean eof = false;
    private boolean firstPartRead = false;

    public MultipartStream(InputStream in, byte[] boundary, long maxSize) {
        this.in = in;
        this.boundary = boundary;
        this.maxSize = maxSize;
    }

    public boolean hasNextPart() throws IOException {
        if (eof) return false;
        System.out.println("📥 hasNextPart(): checking for next boundary...");

        if (!firstPartRead) {
            // skip the initial boundary line
            String line = readLine(in);
            if (line == null || !line.startsWith("--" + new String(boundary, StandardCharsets.UTF_8))) {
                eof = true;
                return false;
            }
            firstPartRead = true;
            return true;
        }

        // Read until we find a boundary line (after previous part)
        String line;
        while ((line = readLine(in)) != null) {
            if (line.equals("--" + new String(boundary, StandardCharsets.UTF_8))) {
                return true;
            }
            if (line.equals("--" + new String(boundary, StandardCharsets.UTF_8) + "--")) {
                eof = true;
                return false;
            }
        }

        eof = true;
        return false;
    }

    public Part nextPart() throws IOException {
        System.out.println("🧩 nextPart(): reading headers for next part...");

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            System.out.println("🔹 Header line: " + line);
            int idx = line.indexOf(':');
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim().toLowerCase(), line.substring(idx + 1).trim());
            }
        }

        System.out.println("✅ Finished reading headers, creating body stream...");
        BoundaryLimitedInputStream blis = new BoundaryLimitedInputStream(in, boundary, maxSize);
        return new Part(headers, blis);
    }

    static class Part {
        private final Map<String, String> headers;
        private final BoundaryLimitedInputStream body;

        Part(Map<String, String> headers, BoundaryLimitedInputStream body) {
            this.headers = headers;
            this.body = body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public InputStream getBodyStream() {
            return body;
        }

        public byte[] readAllBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            body.transferTo(baos);
            return baos.toByteArray();
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int prev = -1;
        int cur;
        while ((cur = in.read()) != -1) {
            if (prev == '\r' && cur == '\n') break;
            if (prev != -1) baos.write(prev);
            prev = cur;
        }
        if (prev != -1 && prev != '\r') baos.write(prev);
        if (baos.size() == 0 && cur == -1) return null;
        return baos.toString(StandardCharsets.UTF_8);
    }
}
