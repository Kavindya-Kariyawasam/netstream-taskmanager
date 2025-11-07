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

        byte[] prefix = "\r\n".getBytes();
        this.lookFor =new byte[prefix.length + boundary.length];
        System.arraycopy(prefix, 0, lookFor, 0, prefix.length);
        System.arraycopy(boundary, 0, lookFor, prefix.length, boundary.length);

    }

    @Override
    public int read() throws IOException {
        if (buffer != null && bufferIndex < buffer.length) {
            return buffer[bufferIndex++] & 0xFF;
        }
        if (ended) return -1;

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

        byte[] winBytes = window.toByteArray();

        if (endsWith(winBytes, lookFor)) {
            // boundary reached
            int outCount = winBytes.length - lookFor.length;
            if (outCount < 0) outCount = 0;
            byte[] outBytes = new byte[outCount];
            System.arraycopy(winBytes, 0, outBytes, 0, outCount);
            buffer = outBytes;
            bufferIndex = 0;
            ended = true;
            if (buffer.length == 0) return -1;
            return buffer[bufferIndex++] & 0xFF;
        }

        // If window larger than lookFor, flush first byte
        if (winBytes.length > lookFor.length) {
            int result = winBytes[0] & 0xFF;
            window.reset();
            window.write(winBytes, 1, winBytes.length - 1);
            return result;
        }

        // otherwise, keep reading more bytes
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
