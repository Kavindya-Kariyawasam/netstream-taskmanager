package nio;

import com.google.gson.Gson;

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
        if (requestLine == null || requestLine.isEmpty()) {
            writeSimpleResponse(out, 400, "text/plain", "Bad Request");
            safeClose();
            return;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            writeSimpleResponse(out, 400, "text/plain", "Bad Request");
            safeClose();
            return;
        }

        String method = parts[0].toUpperCase(Locale.ROOT);
        String path = parts[1];

        Headers headers = Headers.parse(bis);

        // ✅ Handle CORS preflight (OPTIONS request)
        if ("OPTIONS".equals(method)) {
            writeCORSPreflightResponse(out);
            safeClose();
            return;
        }

        try {
            if ("POST".equals(method) && "/upload".equals(path)) {
                FileUploadHandler uploadHandler = new FileUploadHandler(bis, out, headers);
                String savedFileName = uploadHandler.handle();

                if (savedFileName != null) {
                    String responseJson = gson.toJson(new UploadResponse(
                            "success",
                            "File uploaded successfully",
                            "/download/" + savedFileName
                    ));
                    writeSimpleResponse(out, 200, "application/json", responseJson);
                } else {
                    writeSimpleResponse(out, 400, "application/json",
                            gson.toJson(new SimpleResponse("error", "Upload failed"))
                    );
                }

            } else if ("GET".equals(method) && path.startsWith("/download/")) {
                String fileId = path.substring("/download/".length());
                FileDownloadHandler downloadHandler = new FileDownloadHandler(out, fileId, headers);
                downloadHandler.handle();

            } else {
                writeSimpleResponse(out, 404, "application/json",
                        gson.toJson(new SimpleResponse("error", "Not found"))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeSimpleResponse(out, 500, "application/json",
                    gson.toJson(new SimpleResponse("error", "Internal server error: " + e.getMessage()))
            );
        }

        safeClose();
    }

    private void safeClose() {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int value;
        boolean gotCR = false;
        while ((value = in.read()) != -1) {
            if (value == '\r') {
                gotCR = true;
                continue;
            }
            if (value == '\n') break;
            if (gotCR) {
                baos.write('\r');
                gotCR = false;
            }
            baos.write(value);
        }
        if (baos.size() == 0 && value == -1) return null;
        return baos.toString(StandardCharsets.UTF_8);
    }

    // ✅ Include CORS headers in all normal responses
    private static void writeSimpleResponse(OutputStream out, int statusCode, String contentType, String body) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        bw.write("HTTP/1.1 " + statusCode + "\r\n");
        bw.write("Content-Type: " + contentType + "\r\n");
        bw.write("Access-Control-Allow-Origin: *\r\n");
        bw.write("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n");
        bw.write("Access-Control-Allow-Headers: Content-Type\r\n");
        bw.write("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
        bw.write("\r\n");
        bw.write(body);
        bw.flush();
    }

    // ✅ Handle OPTIONS preflight
    private static void writeCORSPreflightResponse(OutputStream out) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        bw.write("HTTP/1.1 204 No Content\r\n");
        bw.write("Access-Control-Allow-Origin: *\r\n");
        bw.write("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n");
        bw.write("Access-Control-Allow-Headers: Content-Type\r\n");
        bw.write("Access-Control-Max-Age: 86400\r\n");
        bw.write("\r\n");
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

    // ✅ Custom response for uploads
    static class UploadResponse extends SimpleResponse {
        String downloadUrl;

        UploadResponse(String status, String message, String downloadUrl) {
            super(status, message);
            this.downloadUrl = downloadUrl;
        }
    }
}
