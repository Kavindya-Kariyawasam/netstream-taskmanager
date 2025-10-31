package nio;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

class RequestRouter {
    private final SocketChannel channel;
    private final Gson gson;

    public RequestRouter(SocketChannel channel, Gson gson) {
        this.channel = channel;
        this.gson = gson;
    }

    public void route() throws IOException {
        InputStream in = Channels.newInputStream(channel);
        OutputStream out = Channels.newOutputStream(channel);
        BufferedInputStream bis = new BufferedInputStream(in);
        bis.mark(8192);

        String requestLine = readLine(bis);
        if(requestLine == null || requestLine.isEmpty()) {
            writeSimpleResponse(out,400,"text/plain","Bad Request");
            safeClose();
            return;
        }

        String[] parts = requestLine.split(" ");
        if(parts.length < 2) {
            writeSimpleResponse(out,400,"text/plain","Bad Request");
            safeClose();
            return;
        }
        String method = parts[0].toUpperCase(Locale.ROOT);
        String path = parts[1];

//        Headers headers = Headers.parse(bis);

//        if ("POST".equals(method) && "/upload".equals(path)) {
//            FileUploadHandler uploadHandler = new FileUploadHandler(bis, out, headers);
//            uploadHandler.handle();
//        } else if ("GET".equals(method) && path.startsWith("/download/")) {
//            String fileId = path.substring("/download/".length());
//            FileDownloadHandler downloadHandler = new FileDownloadHandler(out, fileId, headers);
//            downloadHandler.handle();
//        } else {
//            writeSimpleResponse(out, 404, "application/json", gson.toJson(
//                    new SimpleResponse("error", "Not found")
//            ));
//        }
        //safeClose();
    }
    private void safeClose() {
        try{
            channel.close();
        }catch(IOException ignored){}
    }
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int value;
        boolean gotCR = false;
        while((value = in.read()) != -1) {
            if(value == '\r'){
                gotCR = true;
                continue;
            }
            if(value == '\n') break;
            if(gotCR){
                baos.write('\r');
                gotCR = false;
            }
            baos.write(value);
        }
        if(baos.size() == 0 && value == -1) return null;
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static void writeSimpleResponse(OutputStream out, int statusCode, String contentType, String body) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        bw.write("HTTP/1.1 " + statusCode + "\r\n");
        bw.write("Content-Type: " + contentType + "\r\n");
        bw.write("Content-Length: " + body.getBytes(StandardCharsets.UTF_8) + "\r\n");
        bw.write("\r\n");
        bw.write(body);
        bw.flush();
    }
    static class SimpleResponse {
        String status;
        String message;
        SimpleResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
