package nio;

import com.google.gson.Gson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FileUploadHandler {
    private static final long MAX_FILE_BYTES = 50L * 1024L * 1024L;
    private static final Path STORAGE_DIR = Paths.get("backend","files");
    private static final Map<String,FileMetadata> METADATA_STORE = new ConcurrentHashMap<>();

    private final BufferedInputStream in;
    private final OutputStream out;
    private final Headers headers;
    private final Gson gson = new Gson();

    public FileUploadHandler(BufferedInputStream in, OutputStream out, Headers headers) {
        this.in = in;
        this.out = out;
        this.headers = headers;
        try{
            Files.createDirectories(STORAGE_DIR);
        }catch(IOException e){
            throw new RuntimeException("Unable to create directory: "+e.getMessage(),e);
        }
    }

    public void handle() throws IOException {
        Optional<String> ctOpt = headers.get("Content-Type");
        if (ctOpt.isEmpty() || !ctOpt.get().startsWith("multipart/form-data")) {
            writeJsonResponse(400, Map.of("status ", "error ", "message ", "Content-Type must be multipart/form-data "));
            return;
        }
        String contentType = ctOpt.get();
        String boundary = extractBoundary(contentType);
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

        while(multipart.hasNextPart()){
            MultipartStream.Part part = multipart.nextPart();
            String disposition = part.getHeaders().getOrDefault("Content-Disposition", "");
            String name = parseName(disposition);
            if(name==null) continue;

            if("file".equals(name)){
                originalFileName = parseFileName(disposition);
                if(originalFileName==null) originalFileName = "upload.bin";

                String safeName = santitizeFileName(originalFileName);
                fileId = "file_"+ System.currentTimeMillis()+"_"+ UUID.randomUUID().toString().substring(0,8);
                storedFile = STORAGE_DIR.resolve(fileId+"_"+safeName);

                try(FileChannel fileChannel = FileChannel.open(storedFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)){
                    byte[] buffer = new byte[8192];
                    int read;
                    long bytesWritten = 0;
                    InputStream pis = part.getBodyStream();
                    while((read=pis.read(buffer))!=-1){
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);
                        while (byteBuffer.hasRemaining()) fileChannel.write(byteBuffer);
                        bytesWritten += read;
                        if(bytesWritten>MAX_FILE_BYTES){
                            try{
                                Files.deleteIfExists(storedFile);
                            }catch(IOException ignored){
                                writeJsonResponse(413,Map.of("status", "error", "message", "File too large (Max 50MB)"));
                                return;
                            }
                        }
                    }
                    totalBytes += read;
                }
                FileMetadata meta = new FileMetadata(fileId,originalFileName,storedFile.toString(),totalBytes,formFields.get("taskId"),formFields.get("description"));
                METADATA_STORE.put(fileId,meta);

            }else{
                String value = new String(part.readAllBytes(), StandardCharsets.UTF_8);
                formFields.put(name,value);
            }
        }
        if(fileId == null){
            writeJsonResponse(400, Map.of("status", "error", "message", "No file part 'file' found"));
            return;
        }

        Map<String,Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("fileId", fileId);
        response.put("fileName", originalFileName);
        response.put("size", totalBytes);
        writeJsonResponse(200, response);

    }
    private static String santitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static String parseName(String disposition) {
        for (String part : disposition.split(";")) {
            part = part.trim();
            if (part.startsWith("name=")) {
                String value = stripQuotes(part.substring(5).trim());
                return value;
            }
        }
        return null;
    }

    private static String parseFileName(String disposition) {
        for (String part : disposition.split(";")) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String value = stripQuotes(part.substring(9).trim());
                return value;
            }
        }
        return null;
    }

    private static String stripQuotes(String string) {
        if (string.startsWith("\"") && string.endsWith("\"") && string.length() >= 2) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    private static String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                String boundary = part.substring("boundary=".length());
                if(boundary.startsWith("\"") && boundary.endsWith("\"")){
                    boundary = boundary.substring(1, boundary.length()-1);
                }
                return boundary;
            }
        }
        return null;
    }

    private void writeJsonResponse(int statusCode, Map<String, Object> map) throws IOException {
        String body = gson.toJson(map);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        bufferedWriter.write("HTTP/1.1 "+statusCode+" \r\n");
        bufferedWriter.write("Content-Type: application/json\r\n");
        bufferedWriter.write("Content-Length: "+body.getBytes(StandardCharsets.UTF_8).length+"\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write(body);
        bufferedWriter.flush();
    }

    public static Optional<FileMetadata> getMetadata(String fileId){
        return Optional.ofNullable(METADATA_STORE.get(fileId));
    }
}
