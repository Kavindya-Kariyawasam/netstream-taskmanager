package nio;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal streaming multipart/form-data parser.
 *
 * Usage:
 *  MultipartStream ms = new MultipartStream(inputStream, boundaryBytes, maxSize);
 *  while (ms.hasNextPart()) {
 *      Part p = ms.nextPart();
 *      // p.getHeaders(), p.getBodyStream()
 *  }
 *
 * This implementation reads parts in sequence and prevents loading entire file into memory.
 */
class MultipartStream {

    private final InputStream in;
    private final byte[] boundary;
    private final long maxSize;
    private boolean eof = false;
    private final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);
    private final byte[] DASHDASH = "--".getBytes(StandardCharsets.UTF_8);

    public MultipartStream(InputStream in, byte[] boundary, long maxSize) {
        this.in = in;
        this.boundary = boundary;
        this.maxSize = maxSize;
    }

    public boolean hasNextPart() throws IOException {
        if (eof) return false;
        // read until boundary line
        String line;
        while ((line = readLine(in)) != null) {
            if (line.equals("--" + new String(boundary, StandardCharsets.UTF_8))) return true;
            if (line.equals("--" + new String(boundary, StandardCharsets.UTF_8) + "--")) { eof = true; return false; }
        }
        eof = true;
        return false;
    }

    public Part nextPart() throws IOException {
        // Read headers for part
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim().toLowerCase(), line.substring(idx+1).trim());
            }
        }
        // Now the body stream: it must be read until boundary occurs.
        // We'll provide a limited InputStream that stops at boundary.
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

        public Map<String, String> getHeaders() { return headers; }
        public InputStream getBodyStream() { return body; }
        public byte[] readAllBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            body.transferTo(baos);
            return baos.toByteArray();
        }
    }

    // Helper to read CRLF-terminated lines
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
