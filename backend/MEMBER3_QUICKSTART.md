# Member 3 - URL/URI Service Quick Start Guide

## 🎯 What You've Built

You've implemented the **URL Integration Service**, which handles:

- ✅ External API integration (quotes, weather)
- ✅ Gravatar avatar generation
- ✅ URL validation and parsing
- ✅ File upload/download via HTTP
- ✅ Generic REST API requests

---

## 🚀 How to Run Your Service

### Step 1: Compile the Backend

From the `backend` folder:

```powershell
# Use the compile helper script
powershell -ExecutionPolicy Bypass -File compile.ps1
```

Or manually:

```powershell
# Create file list
Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { '"' + ($_.FullName -replace '\\','/') + '"' } | Out-File -FilePath .\files.txt -Encoding ascii

# Compile
cmd /c "javac -d .\bin -cp .\lib\gson-2.10.1.jar @.\files.txt"
```

### Step 2: Run the Application

```powershell
java -cp "bin;lib/*" Main
```

You should see:

```
[INFO] Starting NetStream TaskManager...
==================================================
[URL Service] Started on port 8082
[URL Service] Ready to handle external API requests
==================================================
[INFO] All servers ready!
[INFO] TCP Server: localhost:8080 (pure socket)
[INFO] HTTP Gateway: localhost:3000 (for browser)
[INFO] URL Service: localhost:8082 (external API integration)
Press Ctrl+C to stop
```

---

## 🧪 Test Your Implementation

### Option 1: Use the PowerShell Test Script (EASIEST)

```powershell
# From the backend folder
powershell -ExecutionPolicy Bypass -File test-url-service.ps1
```

This will automatically test all 8 features!

### Option 2: Use the Java Test Client

```powershell
# Run from backend folder
java -cp "bin;lib/*" url.URLServiceTester
```

### Option 3: Manual Testing with PowerShell

```powershell
# Test getting a motivational quote
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

## 📚 Understanding Your Code

### Main Components

1. **URLIntegrationService.java** - Your main service class

   - Runs on port 8082
   - Handles 8 different actions
   - Uses URL, URLConnection, and HttpURLConnection

2. **Key Features You Implemented:**

#### a) External API Integration

```java
URL url = new URL(QUOTES_API);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
```

#### b) URL Validation

```java
URL url = new URL(urlString);  // Throws MalformedURLException if invalid
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
int responseCode = conn.getResponseCode();  // Check accessibility
```

#### c) URL Parsing

```java
URL url = new URL(urlString);
String protocol = url.getProtocol();
String host = url.getHost();
int port = url.getPort();
String path = url.getPath();
```

#### d) File Download

```java
URL url = new URL(fileUrl);
URLConnection conn = url.openConnection();
InputStream in = conn.getInputStream();
Files.copy(in, outputPath);
```

### Exception Handling You Implemented

1. **MalformedURLException** - Invalid URL format
2. **IOException** - Network errors
3. **SocketTimeoutException** - Connection timeouts

---

## 🎓 Key Concepts Demonstrated

### 1. URL vs URI

- **URL**: Locates a resource (includes how to access it)
- **URI**: Identifies a resource (may not include access method)

### 2. URLConnection Methods

```java
conn.setConnectTimeout(10000);      // Connection timeout
conn.setReadTimeout(10000);         // Read timeout
conn.setRequestMethod("GET");       // HTTP method
conn.setRequestProperty(key, value); // Headers
int status = conn.getResponseCode(); // HTTP status
```

### 3. HTTP Request/Response

```java
// Send GET request
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");

// Read response
BufferedReader reader = new BufferedReader(
    new InputStreamReader(conn.getInputStream())
);
```

---

## 📋 Assignment Checklist

- [x] Implement TCP server for URL service ✅
- [x] Integrate external APIs (quotes, weather) ✅
- [x] Fetch Gravatar profile pictures ✅
- [x] URL parsing and validation ✅
- [x] File upload/download via HTTP ✅
- [x] Handle MalformedURLException ✅
- [x] GET/POST request handling ✅
- [x] URLConnection usage ✅
- [x] JSON-based protocol ✅
- [x] Exception handling ✅
- [x] Timeout management ✅

---

## 🎨 Real-World Use Cases

### 1. Task Dashboard Enhancement

```java
// Get motivational quote for dashboard
{"action":"GET_QUOTE"}
// Response: {"status":"success","data":{"quote":"...","author":"..."}}
```

### 2. User Avatars

```java
// Generate avatar URL from email
{"action":"GET_AVATAR","data":{"email":"user@example.com"}}
// Response: {"avatarUrl":"https://www.gravatar.com/avatar/..."}
```

### 3. Weather Integration

```java
// Show weather on task page
{"action":"GET_WEATHER","data":{"city":"London"}}
// Response: {"temperature":"15°C","description":"Partly cloudy"}
```

### 4. External API Integration

```java
// Fetch data from any REST API
{"action":"FETCH_API","data":{"url":"https://api.example.com/data","method":"GET"}}
```

---

## 🔧 Troubleshooting

### Problem: Service won't start

**Solution:**

```powershell
# Check if port 8082 is in use
netstat -ano | findstr :8082

# Kill the process if needed
taskkill /PID <PID> /F
```

### Problem: MalformedURLException

**Solution:**

- Ensure URLs start with `http://` or `https://`
- Check for special characters that need encoding
- Use `URLEncoder.encode()` for query parameters

### Problem: Can't connect to external APIs

**Solution:**

- Check internet connection
- Verify firewall settings
- Try the URL in a web browser first
- Check if API is down

---

## 📖 Important Concepts for Your Report

### 1. URL Class

- Represents a Uniform Resource Locator
- Parses URL components (protocol, host, port, path)
- Validates URL format

### 2. URLConnection

- Abstract class for URL communication
- Handles HTTP headers and timeouts
- Provides input/output streams

### 3. HttpURLConnection

- HTTP-specific features
- Request methods (GET, POST, PUT, DELETE)
- Response codes and headers

### 4. Exception Handling

- MalformedURLException: Invalid URL format
- IOException: Network communication errors
- SocketTimeoutException: Connection timeouts

### 5. Timeout Management

- Connect timeout: Maximum time to establish connection
- Read timeout: Maximum time to wait for data

---

## 🎯 What to Present

### For Your Assignment/Presentation:

1. **Show the code structure**

   - `URLIntegrationService.java` - Main service
   - 8 different actions implemented
   - Exception handling

2. **Demonstrate functionality**

   - Run the test script
   - Show quotes API working
   - Display Gravatar avatar generation
   - Demonstrate URL validation

3. **Explain key concepts**

   - URL vs URI
   - URLConnection usage
   - Exception handling
   - HTTP request/response

4. **Show integration**
   - How it fits with other services
   - JSON protocol
   - Error handling

---

## 📚 Additional Resources

### Official Documentation

- [Java URL Class](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URL.html)
- [URLConnection](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URLConnection.html)
- [HttpURLConnection](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/HttpURLConnection.html)

### APIs Used

- **Quotable API**: https://github.com/lukePeavey/quotable
- **Gravatar**: https://docs.gravatar.com/
- **Wttr.in**: https://github.com/chubin/wttr.in

---

## ✨ Next Steps

1. **Test all features** using the test script
2. **Read the detailed guide** in `URL_SERVICE_GUIDE.md`
3. **Understand the code** - Add comments if needed
4. **Prepare your presentation** - Focus on key concepts
5. **Integrate with frontend** - If required

---

## 💡 Tips for Success

1. **Understand the flow:**

   - Client sends JSON request → Service processes → Returns JSON response

2. **Know your exceptions:**

   - MalformedURLException = Bad URL format
   - IOException = Network error
   - SocketTimeoutException = Took too long

3. **Key methods to remember:**

   - `new URL(string)` - Create URL object
   - `url.openConnection()` - Get connection
   - `conn.getResponseCode()` - Check status
   - `conn.getInputStream()` - Read response

4. **Test thoroughly:**
   - Use the PowerShell script
   - Try different scenarios
   - Test error cases

---

## 🎓 You've Successfully Implemented:

✅ TCP server for external integrations  
✅ Multiple external API integrations  
✅ URL/URI parsing and validation  
✅ File upload/download  
✅ Comprehensive exception handling  
✅ JSON-based communication protocol  
✅ Timeout management  
✅ Real-world use cases

**Great job! You're ready to demonstrate Member 3's responsibilities!** 🎉

---

**Need Help?**

- Check `URL_SERVICE_GUIDE.md` for detailed documentation
- Review `URLIntegrationService.java` code comments
- Run `test-url-service.ps1` to verify everything works
