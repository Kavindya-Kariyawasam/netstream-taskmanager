# URL/URI Integration Service - Member 3 Documentation

## 📋 Overview

The **URL Integration Service** is responsible for handling external API integrations, URL/URI parsing and validation, and file upload/download operations using Java's URL/URLConnection classes.

**Port:** 8082  
**Protocol:** TCP (JSON-based communication)  
**Member:** 3 - URLs/URIs & URLConnection

---

## 🎯 Responsibilities

1. ✅ Integrate with external REST APIs (quotes, weather, etc.)
2. ✅ Fetch user profile pictures from Gravatar
3. ✅ URL parsing and validation
4. ✅ File upload/download via HTTP
5. ✅ Handle MalformedURLException and network errors
6. ✅ GET/POST request handling with URLConnection

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 17+
- Gson library (gson-2.10.1.jar)
- Internet connection (for external API calls)

### Running the Service

The URL service starts automatically when you run `Main.java`, or you can run it standalone:

```java
URLIntegrationService service = new URLIntegrationService(8082);
Thread serviceThread = new Thread(() -> service.start());
serviceThread.start();
```

---

## 📡 API Endpoints

### 1. GET_QUOTE - Fetch Motivational Quote

Fetches a random motivational quote from quotable.io API.

**Request:**

```json
{
  "action": "GET_QUOTE"
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "quote": "The only way to do great work is to love what you do.",
    "author": "Steve Jobs",
    "source": "quotable.io"
  }
}
```

**Java Example:**

```java
Socket socket = new Socket("localhost", 8082);
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

out.println("{\"action\":\"GET_QUOTE\"}");
out.println();

String response = in.readLine();
System.out.println(response);
```

**PowerShell Example:**

```powershell
$client = New-Object System.Net.Sockets.TcpClient("localhost", 8082)
$stream = $client.GetStream()
$writer = New-Object System.IO.StreamWriter($stream)
$reader = New-Object System.IO.StreamReader($stream)

$writer.WriteLine('{"action":"GET_QUOTE"}')
$writer.WriteLine()
$writer.Flush()

$response = $reader.ReadLine()
Write-Host $response

$client.Close()
```

---

### 2. GET_AVATAR - Generate Gravatar Avatar URL

Generates a Gravatar avatar URL from an email address using MD5 hashing.

**Request:**

```json
{
  "action": "GET_AVATAR",
  "data": {
    "email": "user@example.com"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "email": "user@example.com",
    "avatarUrl": "https://www.gravatar.com/avatar/b58996c504c5638798eb6b511e6f49af?s=200&d=identicon",
    "hash": "b58996c504c5638798eb6b511e6f49af",
    "note": "Default size: 200px, Default image: identicon"
  }
}
```

**Use Cases:**

- Display user avatars in task assignments
- Profile pictures in the frontend
- Team member identification

---

### 3. VALIDATE_URL - Validate and Test URL

Validates URL format and tests if it's accessible.

**Request:**

```json
{
  "action": "VALIDATE_URL",
  "data": {
    "url": "https://www.google.com"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "url": "https://www.google.com",
    "valid": true,
    "protocol": "https",
    "host": "www.google.com",
    "port": 443,
    "path": "",
    "accessible": true,
    "httpStatus": 200
  }
}
```

**Error Example (Invalid URL):**

```json
{
  "status": "success",
  "data": {
    "url": "not-a-valid-url",
    "valid": false,
    "accessible": false,
    "error": "Malformed URL: no protocol: not-a-valid-url"
  }
}
```

---

### 4. PARSE_URL - Extract URL Components

Parses a URL and extracts all its components.

**Request:**

```json
{
  "action": "PARSE_URL",
  "data": {
    "url": "https://example.com:8080/path/to/resource?param1=value1&param2=value2#section"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "originalUrl": "https://example.com:8080/path/to/resource?param1=value1&param2=value2#section",
    "protocol": "https",
    "host": "example.com",
    "port": 8080,
    "path": "/path/to/resource",
    "query": "param1=value1&param2=value2",
    "ref": "section",
    "authority": "example.com:8080",
    "userInfo": null,
    "file": "/path/to/resource?param1=value1&param2=value2",
    "queryParams": {
      "param1": "value1",
      "param2": "value2"
    }
  }
}
```

**Use Cases:**

- Validate external resource links in tasks
- Parse attachment URLs
- Extract query parameters from API endpoints

---

### 5. DOWNLOAD_FILE - Download File from URL

Downloads a file from a given URL to the server's upload directory.

**Request:**

```json
{
  "action": "DOWNLOAD_FILE",
  "data": {
    "url": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
    "fileName": "downloaded_doc.pdf"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "fileName": "downloaded_doc.pdf",
    "filePath": "backend/uploads/downloaded_doc.pdf",
    "fileSize": 13264,
    "contentType": "application/pdf",
    "sourceUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
  }
}
```

**Note:** If `fileName` is not provided, it will be extracted from the URL or auto-generated.

---

### 6. UPLOAD_FILE - Upload File to Server

Saves file data to the server's upload directory.

**Request:**

```json
{
  "action": "UPLOAD_FILE",
  "data": {
    "fileData": "This is the file content",
    "fileName": "myfile.txt"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "fileName": "myfile.txt",
    "filePath": "backend/uploads/myfile.txt",
    "fileSize": 24,
    "uploadedAt": 1730734800000
  }
}
```

**Note:** For binary files, encode `fileData` as Base64 string.

---

### 7. GET_WEATHER - Fetch Weather Information

Fetches current weather for a specified city using wttr.in API.

**Request:**

```json
{
  "action": "GET_WEATHER",
  "data": {
    "city": "London"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "city": "London",
    "temperature": "15°C",
    "feelsLike": "14°C",
    "description": "Partly cloudy",
    "humidity": "76%",
    "windSpeed": "13 km/h"
  }
}
```

**Use Cases:**

- Display weather on task dashboard
- Weather-based task reminders
- Location-aware notifications

---

### 8. FETCH_API - Generic API Request

Sends a GET or POST request to any external API.

**Request:**

```json
{
  "action": "FETCH_API",
  "data": {
    "url": "https://jsonplaceholder.typicode.com/todos/1",
    "method": "GET"
  }
}
```

**Response:**

```json
{
  "status": "success",
  "data": {
    "url": "https://jsonplaceholder.typicode.com/todos/1",
    "method": "GET",
    "statusCode": 200,
    "response": "{\"userId\":1,\"id\":1,\"title\":\"delectus aut autem\",\"completed\":false}",
    "success": true
  }
}
```

**Use Cases:**

- Integrate with custom REST APIs
- Fetch data from third-party services
- Test external endpoints

---

## 🔧 Technical Implementation

### Key Java Classes Used

1. **URL** - Represents a Uniform Resource Locator

   ```java
   URL url = new URL("https://api.example.com/data");
   ```

2. **URI** - More flexible URL parsing

   ```java
   URI uri = new URI("https://example.com/path?query=value");
   ```

3. **URLConnection** - Abstract connection to URL resources

   ```java
   URLConnection conn = url.openConnection();
   conn.setConnectTimeout(10000);
   ```

4. **HttpURLConnection** - HTTP-specific features
   ```java
   HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
   httpConn.setRequestMethod("GET");
   int responseCode = httpConn.getResponseCode();
   ```

### Exception Handling

The service handles the following exceptions:

1. **MalformedURLException** - Invalid URL format

   ```java
   try {
       URL url = new URL(urlString);
   } catch (MalformedURLException e) {
       return JsonUtils.createErrorResponse("Invalid URL: " + e.getMessage());
   }
   ```

2. **IOException** - Network communication errors

   ```java
   try {
       URLConnection conn = url.openConnection();
       // ... read data
   } catch (IOException e) {
       return JsonUtils.createErrorResponse("Network error: " + e.getMessage());
   }
   ```

3. **SocketTimeoutException** - Connection timeouts
   ```java
   conn.setConnectTimeout(10000); // 10 seconds
   conn.setReadTimeout(10000);
   ```

### Timeout Configuration

```java
private static final int TIMEOUT = 10000; // 10 seconds

HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setConnectTimeout(TIMEOUT);
conn.setReadTimeout(TIMEOUT);
```

---

## 🧪 Testing

### Using the Test Client

```bash
# From backend directory
java -cp "bin;lib/*" url.URLServiceTester
```

### Manual Testing with PowerShell

```powershell
# Test GET_QUOTE
$client = New-Object System.Net.Sockets.TcpClient("localhost", 8082)
$stream = $client.GetStream()
$writer = New-Object System.IO.StreamWriter($stream)
$reader = New-Object System.IO.StreamReader($stream)

$request = '{"action":"GET_QUOTE"}'
$writer.WriteLine($request)
$writer.WriteLine()
$writer.Flush()

$response = $reader.ReadLine()
Write-Host "Response: $response"

$client.Close()
```

### Manual Testing with Java

```java
import java.io.*;
import java.net.Socket;

public class QuickTest {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8082);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );

        // Send request
        out.println("{\"action\":\"GET_QUOTE\"}");
        out.println();

        // Read response
        String response = in.readLine();
        System.out.println("Response: " + response);

        socket.close();
    }
}
```

---

## 🎨 Integration Examples

### Frontend Integration (JavaScript/TypeScript)

```typescript
// Connect to URL service via TCP
async function getMotivationalQuote() {
  const request = {
    action: "GET_QUOTE",
  };

  const response = await fetch("http://localhost:3000/url-service", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });

  const data = await response.json();
  return data.data; // { quote: "...", author: "..." }
}

// Get Gravatar avatar
async function getUserAvatar(email: string) {
  const request = {
    action: "GET_AVATAR",
    data: { email },
  };

  const response = await fetch("http://localhost:3000/url-service", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });

  const data = await response.json();
  return data.data.avatarUrl;
}
```

### Task Dashboard Enhancement

```java
// Add motivational quote to task dashboard
public class TaskDashboard {
    public void displayQuote() {
        URLIntegrationService urlService = new URLIntegrationService(8082);

        // Get quote
        String quote = urlService.getMotivationalQuote();
        System.out.println("Today's Motivation: " + quote);
    }

    public void showWeather(String city) {
        // Get weather for user's location
        String weather = urlService.getWeatherInfo(city);
        System.out.println("Current Weather: " + weather);
    }
}
```

---

## 📊 Performance Considerations

### Timeout Settings

- **Connect Timeout:** 10 seconds
- **Read Timeout:** 10 seconds
- Prevents hanging on unresponsive APIs

### File Size Limits

- Maximum recommended file size: 50MB
- Larger files should use the NIO service (Member 5)

### API Rate Limiting

- Some external APIs have rate limits
- Consider implementing caching for frequently accessed data
- Example: Cache quotes for 1 hour

### Concurrent Requests

The service can handle multiple simultaneous API requests through the thread pool:

```java
ExecutorService pool = ThreadPoolManager.getThreadPool();
pool.submit(() -> handleClient(clientSocket));
```

---

## 🔒 Security Best Practices

### 1. URL Validation

Always validate URLs before processing:

```java
private boolean isValidUrl(String url) {
    try {
        new URL(url);
        return true;
    } catch (MalformedURLException e) {
        return false;
    }
}
```

### 2. Whitelist Allowed Protocols

```java
if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
    throw new SecurityException("Only HTTP/HTTPS protocols allowed");
}
```

### 3. File Upload Security

- Validate file types
- Limit file sizes
- Sanitize file names
- Store in isolated directory

### 4. Prevent SSRF Attacks

```java
// Block private IP ranges
if (isPrivateIP(url.getHost())) {
    throw new SecurityException("Access to private IPs not allowed");
}
```

---

## 🐛 Troubleshooting

### Issue: "Connection refused" error

**Solution:**

1. Ensure the URL service is running on port 8082
2. Check if port is already in use: `netstat -ano | findstr :8082`
3. Verify firewall settings

### Issue: "MalformedURLException"

**Solution:**

- Check URL format: must include protocol (http:// or https://)
- Ensure special characters are properly encoded
- Use `URLEncoder.encode()` for query parameters

### Issue: "Timeout" errors

**Solution:**

- Check internet connection
- Verify external API is accessible
- Increase timeout values if needed
- Handle timeouts gracefully in client code

### Issue: File download fails

**Solution:**

- Verify URL is accessible
- Check file permissions in `uploads/` directory
- Ensure sufficient disk space
- Validate Content-Type header

---

## 📚 External APIs Used

### 1. Quotable API

- **URL:** https://api.quotable.io/random
- **Type:** Free, no API key required
- **Docs:** https://github.com/lukePeavey/quotable

### 2. Gravatar

- **URL:** https://www.gravatar.com/avatar/{hash}
- **Type:** Free, no API key required
- **Docs:** https://docs.gravatar.com/

### 3. Wttr.in Weather API

- **URL:** https://wttr.in/{city}?format=j1
- **Type:** Free, no API key required
- **Docs:** https://github.com/chubin/wttr.in

---

## 🎓 Learning Resources

### Java URL/URLConnection Documentation

- [URL Class](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URL.html)
- [URLConnection](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URLConnection.html)
- [HttpURLConnection](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/HttpURLConnection.html)

### Key Concepts

1. **URL vs URI:**

   - URL: Locates a resource (includes access mechanism)
   - URI: Identifies a resource (may not include access method)

2. **URLConnection Methods:**

   - `setRequestMethod()` - GET, POST, PUT, DELETE
   - `setRequestProperty()` - Set headers
   - `getInputStream()` - Read response
   - `getOutputStream()` - Send data (POST)

3. **Response Handling:**
   - `getResponseCode()` - HTTP status code
   - `getContentType()` - MIME type
   - `getContentLength()` - Size in bytes

---

## ✅ Implementation Checklist

- [x] TCP server on port 8082
- [x] JSON request/response protocol
- [x] Motivational quotes API integration
- [x] Gravatar avatar URL generation
- [x] URL validation and parsing
- [x] File download from URLs
- [x] File upload handling
- [x] Weather API integration
- [x] Generic API fetcher
- [x] MalformedURLException handling
- [x] IOException handling
- [x] Timeout management
- [x] GET/POST request support
- [x] URLConnection usage
- [x] HttpURLConnection usage

---

## 🚀 Next Steps

1. **Test all endpoints** using the provided test client
2. **Integrate with frontend** - Add avatar display and quotes to UI
3. **Add caching** - Cache frequently accessed API responses
4. **Implement authentication** - For APIs that require API keys
5. **Add more APIs** - Time zones, currency conversion, etc.
6. **Monitor performance** - Log API response times
7. **Error logging** - Implement comprehensive error tracking

---

## 📞 Support

For questions or issues:

1. Check the troubleshooting section above
2. Review the code comments in `URLIntegrationService.java`
3. Test with the provided `URLServiceTester.java`
4. Verify external APIs are accessible

---

**Last Updated:** November 4, 2025  
**Version:** 1.0  
**Author:** Member 3 - URL/URI Integration Service
