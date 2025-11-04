package url;

import java.io.*;
import java.net.Socket;

/**
 * Test client for URL Integration Service
 * Demonstrates all available features
 */
public class URLServiceTester {
    private static final String HOST = "localhost";
    private static final int PORT = 8082;

    public static void main(String[] args) {
        URLServiceTester tester = new URLServiceTester();

        System.out.println("=".repeat(60));
        System.out.println("URL Integration Service - Test Client");
        System.out.println("=".repeat(60));

        // Test 1: Get Motivational Quote
        System.out.println("\n[TEST 1] Fetching motivational quote...");
        tester.testGetQuote();

        // Test 2: Get Gravatar Avatar
        System.out.println("\n[TEST 2] Generating Gravatar avatar URL...");
        tester.testGetAvatar("test@example.com");

        // Test 3: Validate URL
        System.out.println("\n[TEST 3] Validating URL...");
        tester.testValidateURL("https://www.google.com");

        // Test 4: Parse URL
        System.out.println("\n[TEST 4] Parsing URL components...");
        tester.testParseURL("https://example.com:8080/path/to/resource?param1=value1&param2=value2#section");

        // Test 5: Download File
        System.out.println("\n[TEST 5] Downloading file...");
        tester.testDownloadFile("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "test.pdf");

        // Test 6: Upload File
        System.out.println("\n[TEST 6] Uploading file...");
        tester.testUploadFile("Sample file content", "test_upload.txt");

        // Test 7: Get Weather
        System.out.println("\n[TEST 7] Fetching weather information...");
        tester.testGetWeather("London");

        // Test 8: Fetch from Generic API
        System.out.println("\n[TEST 8] Fetching from generic API...");
        tester.testFetchAPI("https://jsonplaceholder.typicode.com/todos/1", "GET");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("All tests completed!");
        System.out.println("=".repeat(60));
    }

    private void testGetQuote() {
        String request = "{\"action\":\"GET_QUOTE\"}";
        sendRequest(request);
    }

    private void testGetAvatar(String email) {
        String request = String.format(
                "{\"action\":\"GET_AVATAR\",\"data\":{\"email\":\"%s\"}}",
                email);
        sendRequest(request);
    }

    private void testValidateURL(String url) {
        String request = String.format(
                "{\"action\":\"VALIDATE_URL\",\"data\":{\"url\":\"%s\"}}",
                url);
        sendRequest(request);
    }

    private void testParseURL(String url) {
        String request = String.format(
                "{\"action\":\"PARSE_URL\",\"data\":{\"url\":\"%s\"}}",
                url);
        sendRequest(request);
    }

    private void testDownloadFile(String url, String fileName) {
        String request = String.format(
                "{\"action\":\"DOWNLOAD_FILE\",\"data\":{\"url\":\"%s\",\"fileName\":\"%s\"}}",
                url, fileName);
        sendRequest(request);
    }

    private void testUploadFile(String fileData, String fileName) {
        String request = String.format(
                "{\"action\":\"UPLOAD_FILE\",\"data\":{\"fileData\":\"%s\",\"fileName\":\"%s\"}}",
                fileData, fileName);
        sendRequest(request);
    }

    private void testGetWeather(String city) {
        String request = String.format(
                "{\"action\":\"GET_WEATHER\",\"data\":{\"city\":\"%s\"}}",
                city);
        sendRequest(request);
    }

    private void testFetchAPI(String url, String method) {
        String request = String.format(
                "{\"action\":\"FETCH_API\",\"data\":{\"url\":\"%s\",\"method\":\"%s\"}}",
                url, method);
        sendRequest(request);
    }

    private void sendRequest(String request) {
        try (
                Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            // Send request
            out.println(request);
            out.println(); // Empty line to indicate end of request

            // Read response
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            System.out.println("Response: " + formatJson(response.toString()));

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure the URL Service is running on port " + PORT);
        }
    }

    private String formatJson(String json) {
        // Simple formatting for readability
        if (json.length() > 200) {
            return json.substring(0, 200) + "... (truncated)";
        }
        return json;
    }
}
