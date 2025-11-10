# URL Service Implementation Examples

# Demonstrating URL/URLConnection concepts

## Example 1: Basic URL Creation and Parsing

```java
import java.net.URL;
import java.net.URI;

public class URLBasics {
    public static void main(String[] args) throws Exception {
        // Creating a URL
        URL url = new URL("https://api.example.com:8080/users?id=123#profile");

        // Extracting components
        System.out.println("Protocol: " + url.getProtocol());    // https
        System.out.println("Host: " + url.getHost());            // api.example.com
        System.out.println("Port: " + url.getPort());            // 8080
        System.out.println("Path: " + url.getPath());            // /users
        System.out.println("Query: " + url.getQuery());          // id=123
        System.out.println("Ref: " + url.getRef());              // profile
    }
}
```

## Example 2: Making HTTP GET Request

```java
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HTTPGetExample {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://api.quotable.io/random");

        // Open connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Configure request
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");

        // Get response code
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read response
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );

            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            System.out.println("Response: " + response.toString());
        }
    }
}
```

## Example 3: Exception Handling

```java
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

public class ExceptionHandlingExample {
    public static void validateURL(String urlString) {
        try {
            // This may throw MalformedURLException
            URL url = new URL(urlString);

            // This may throw IOException
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);

            int responseCode = conn.getResponseCode();
            System.out.println("URL is valid and accessible: " + responseCode);

        } catch (MalformedURLException e) {
            System.err.println("Invalid URL format: " + e.getMessage());
            // Handle: malformed URL

        } catch (SocketTimeoutException e) {
            System.err.println("Connection timeout: " + e.getMessage());
            // Handle: server took too long to respond

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
            // Handle: network issues, DNS failures, etc.
        }
    }
}
```

## Example 4: Downloading Files

```java
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileDownloadExample {
    public static void downloadFile(String fileUrl, String savePath) throws Exception {
        URL url = new URL(fileUrl);

        // Open connection
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(10000);

        // Get file info
        long fileSize = conn.getContentLengthLong();
        String contentType = conn.getContentType();

        System.out.println("Downloading file...");
        System.out.println("Size: " + fileSize + " bytes");
        System.out.println("Type: " + contentType);

        // Download
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Download complete!");
    }
}
```

## Example 5: Working with Query Parameters

```java
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class QueryParameterExample {
    public static void main(String[] args) throws Exception {
        // Building URL with query parameters
        String baseUrl = "https://api.example.com/search";
        String query = "hello world";
        String category = "books";

        // Encode parameters
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);

        String fullUrl = baseUrl + "?q=" + encodedQuery + "&category=" + encodedCategory;
        System.out.println("Full URL: " + fullUrl);

        // Parsing query parameters
        URL url = new URL(fullUrl);
        String queryString = url.getQuery();

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            System.out.println(key + " = " + value);
        }
    }
}
```

## Example 6: Setting Request Headers

```java
import java.net.URL;
import java.net.HttpURLConnection;

public class RequestHeadersExample {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://api.github.com/users/octocat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set request headers
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "MyApp/1.0");
        conn.setRequestProperty("Authorization", "Bearer YOUR_TOKEN");

        // Get response headers
        int responseCode = conn.getResponseCode();
        String contentType = conn.getContentType();
        String encoding = conn.getContentEncoding();

        System.out.println("Response Code: " + responseCode);
        System.out.println("Content-Type: " + contentType);
        System.out.println("Encoding: " + encoding);
    }
}
```

## Example 7: POST Request with Data

```java
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class POSTRequestExample {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://httpbin.org/post");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Configure for POST
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        // Prepare data
        String jsonData = "{\"name\":\"John\",\"age\":30}";
        byte[] postData = jsonData.getBytes(StandardCharsets.UTF_8);

        // Send data
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData);
        }

        // Get response
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
    }
}
```

## Example 8: Timeout Configuration

```java
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

public class TimeoutExample {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://slow-api.example.com/data");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set timeouts
            conn.setConnectTimeout(5000);  // 5 seconds to connect
            conn.setReadTimeout(10000);    // 10 seconds to read data

            int responseCode = conn.getResponseCode();
            System.out.println("Response: " + responseCode);

        } catch (SocketTimeoutException e) {
            System.err.println("Timeout! Server too slow.");
            // Handle timeout gracefully

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## Example 9: URL Validation

```java
import java.net.URL;
import java.net.MalformedURLException;

public class URLValidation {
    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);

            // Additional checks
            String protocol = url.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                return false;
            }

            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }

            return true;

        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String[] urls = {
            "https://www.google.com",
            "http://localhost:8080/path",
            "not-a-url",
            "ftp://example.com",
            "https://"
        };

        for (String url : urls) {
            System.out.println(url + " -> " + (isValidURL(url) ? "Valid" : "Invalid"));
        }
    }
}
```

## Key Takeaways

### 1. URL Class

- Parses and represents URLs
- Extracts components (protocol, host, port, path, query)
- Validates URL format

### 2. URLConnection

- Opens connection to URL
- Sets timeouts and headers
- Reads/writes data

### 3. HttpURLConnection

- HTTP-specific features
- Request methods (GET, POST, etc.)
- Response codes and headers

### 4. Exception Handling

- `MalformedURLException` - Bad URL format
- `IOException` - Network errors
- `SocketTimeoutException` - Timeouts

### 5. Best Practices

- Always set timeouts
- Use try-with-resources for streams
- Encode query parameters
- Validate URLs before use
- Handle exceptions gracefully
