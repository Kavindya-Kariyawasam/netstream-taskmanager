package nio;

import java.io.*;

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

        if(endsWith(window.toByteArray(),lookFor)){
            byte[] windowBytes = window.toByteArray();
            int outCount = windowBytes.length - lookFor.length;

            byte[] outBytes = new byte[outCount];
            ended = true;

            this.buffer = outBytes;
            this.bufferIndex = 0;
            return read();

        }
        if(window.size() > lookFor.length){
            byte[] windowArray  = window.toByteArray();
            int returnBytes = windowArray[0] & 0xFF;

            window.reset();
            window.write(windowArray, 1, windowArray.length - 1);
            return returnBytes;
        }
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

    // Helper to transfer fully
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
