package nio;

import java.io.*;
import java.util.Arrays;

public class BoundaryLimitedInputStream extends InputStream {
    private final InputStream in;
    private final byte[] boundary;
    private final byte[] lookFor;
    private final long maxBytes;
    private final ByteArrayOutputStream window = new ByteArrayOutputStream();
    private boolean ended = false;
    private long readSoFar = 0;

    public BoundaryLimitedInputStream(InputStream in, byte[] boundary, long maxBytes) {
        this.in = in;
        this.boundary = boundary;
        this.maxBytes = maxBytes;

    // Multipart part boundary in the stream appears as CRLF + "--" + boundary
    byte[] prefix = "\r\n--".getBytes();
    this.lookFor = new byte[prefix.length + boundary.length];
    System.arraycopy(prefix, 0, lookFor, 0, prefix.length);
    System.arraycopy(boundary, 0, lookFor, prefix.length, boundary.length);

    }

    @Override
    public int read() throws IOException {
        // If we've already reached the end (boundary found and buffer consumed)
        if (ended && buffer == null) return -1;

        // If there are bytes buffered to return (from when we detected the boundary), serve them first
        if (buffer != null) {
            if (bufferIndex < buffer.length) {
                return buffer[bufferIndex++] & 0xFF;
            } else {
                // buffer fully consumed
                buffer = null;
                bufferIndex = 0;
                // If ended flag is set, signal EOF
                if (ended) return -1;
            }
        }

        int currentByte = in.read();
        if (currentByte == -1) {
            ended = true;
            return -1;
        }

        readSoFar++;
        if (readSoFar > maxBytes) {
            throw new IOException("Part exceeds maximum allowed size");
        }

        window.write(currentByte);

        byte[] windowBytes = window.toByteArray();
        // Check if the lookFor sequence (CRLF + boundary) is at the end of the window
        if (endsWith(windowBytes, lookFor)) {
            int outCount = windowBytes.length - lookFor.length;
            if (outCount > 0) {
                // copy bytes before the boundary into buffer to be returned
                buffer = Arrays.copyOf(windowBytes, outCount);
                bufferIndex = 0;
            } else {
                buffer = null;
            }
            ended = true;
            // clear window
            window.reset();
            // serve first byte from buffer (if present)
            if (buffer != null) {
                return buffer[bufferIndex++] & 0xFF;
            }
            return -1;
        }

        // If the window has grown larger than the lookFor sequence, we can emit the earliest byte
        if (window.size() > lookFor.length) {
            byte[] wa = window.toByteArray();
            int out = wa[0] & 0xFF;
            // shift window left by one
            window.reset();
            if (wa.length - 1 > 0) {
                window.write(wa, 1, wa.length - 1);
            }
            return out;
        }

        // Need more bytes to decide — read again
        return read();
    }

    private byte[] buffer = null;
    private int bufferIndex = 0;

    @Override
    public int read(byte[] currentByte, int off, int len) throws IOException{
        int i = 0;
        for (; i < len; i++) {
            int val = read();
            if (val == -1) {
                return i == 0 ? -1 : i;
            }
            currentByte[off + i] = (byte) val;
        }
        return i;
    }

    private static boolean endsWith(byte[] haystack, byte[] needle) {
        if (haystack.length < needle.length) return false;
        for (int i = 0; i < needle.length; i++) {
            if (haystack[haystack.length - needle.length + i] != needle[i]) return false;
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int r;
        long total = 0;

        while ((r = read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, r);
            total += r;
        }

        return total;
    }

}
