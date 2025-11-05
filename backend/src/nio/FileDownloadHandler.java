package nio;

import com.google.gson.Gson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileDownloadHandler {
    private final OutputStream outputStream;
    private final String fieldId;
    private final Headers headers;
    private final Gson gson = new Gson();

    public FileDownloadHandler(OutputStream outputStream, String fieldId, Headers headers) {
        this.outputStream = outputStream;
        this.fieldId = fieldId;
        this.headers = headers;
    }

    public void handle() throws IOException {
        Optional<FileMetadata> metaOpt = FileUploadHandler.getMetadata(fieldId);
        if (metaOpt.isEmpty()) {
            writeJsonResponse(404,"error","File missing on server");
            return;
        }
        FileMetadata meta = metaOpt.get();
        Path path = Paths.get(meta.getStoredPath());
        if(!path.toFile().exists()) {
            writeJsonResponse(404,"error","File missing on server");
            return;
        }

        try(FileChannel fileChannel = FileChannel.open(path)) {
            long size = fileChannel.size();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            String header = "HTTP/1.1 200 OK\r\n"+
                    "Content-Type: application/octet-stream\r\n"+
                    "Content-Disposition: attachment; filename =\""+meta.getOriginalName()+"\"\r\n"+
                    "Content-Length: " + size + "\r\n" +
                    "\r\n";
            bufferedOutputStream.write(header.getBytes());
            bufferedOutputStream.flush();

            ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
            long position = 0;
            while (position < size) {
                buffer.clear();
                int read = fileChannel.read(buffer);
                buffer.flip();
                byte[] tmp = new byte[buffer.remaining()];
                buffer.get(tmp);
                bufferedOutputStream.write(tmp);
                position += read;
            }
            bufferedOutputStream.flush();
        }
    }

    private void writeJsonResponse(int statusCode,String status, String message) throws IOException {
        String body = gson.toJson(new Simple(status, message));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write("HTTP/1.1 "+statusCode+"\r\n");
        bufferedWriter.write("Content-Type: application/json\r\n");
        bufferedWriter.write("Content-Length: "+body.length()+"\r\n");
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
