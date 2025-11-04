# 🚀 Member 3 - Getting Started

Hi! This is your complete guide to understanding and running your URL/URI Integration Service implementation.

---

## ⚡ Super Quick Start (3 Commands)

```powershell
# 1. Navigate to backend folder
cd c:\Users\Thilinika\Desktop\Me\Network\netstream-taskmanager\backend

# 2. Run the application
java -cp "bin;lib/*" Main

# 3. In another terminal, test it
powershell -ExecutionPolicy Bypass -File test-url-service.ps1
```

That's it! You should see all 8 features working! 🎉

---

## 📚 What Did You Just Build?

You implemented **Member 3** - the URL/URI External Integration Service. Here's what it does:

### 🎯 Core Features

1. **GET_QUOTE** - Fetch motivational quotes from the internet
2. **GET_AVATAR** - Generate Gravatar avatar URLs from emails
3. **VALIDATE_URL** - Check if URLs are valid and accessible
4. **PARSE_URL** - Extract all components from a URL
5. **DOWNLOAD_FILE** - Download files from the internet
6. **UPLOAD_FILE** - Save files to the server
7. **GET_WEATHER** - Get weather information for any city
8. **FETCH_API** - Call any REST API on the internet

### 🔧 Technical Skills Demonstrated

- ✅ TCP server programming
- ✅ URL/URLConnection usage
- ✅ HTTP GET/POST requests
- ✅ JSON request/response handling
- ✅ Exception handling (MalformedURLException, IOException, etc.)
- ✅ File I/O operations
- ✅ External API integration

---

## 🎓 Understanding Your Code

### Main File: `URLIntegrationService.java`

```java
// 1. It's a TCP server on port 8082
ServerSocket serverSocket = new ServerSocket(8082);

// 2. Accepts client connections
Socket clientSocket = serverSocket.accept();

// 3. Reads JSON requests
JsonObject request = JsonParser.parseString(json).getAsJsonObject();

// 4. Routes to appropriate handler
switch (action) {
    case "GET_QUOTE": return getMotivationalQuote();
    case "GET_AVATAR": return getGravatarAvatar(email);
    // ... etc
}

// 5. Makes HTTP requests to external APIs
URL url = new URL("https://api.quotable.io/random");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();

// 6. Handles exceptions properly
catch (MalformedURLException e) {
    return JsonUtils.createErrorResponse("Invalid URL");
}
```

---

## 🧪 How to Test

### Option 1: Automated Test (Easiest!)

```powershell
powershell -ExecutionPolicy Bypass -File test-url-service.ps1
```

This will:

- Test all 8 features
- Show colored output
- Display results clearly

### Option 2: Test Individual Features

```powershell
# Get a motivational quote
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

## 📖 Key Java Concepts

### 1. Creating URLs

```java
URL url = new URL("https://api.example.com/data");
```

**Important:** This can throw `MalformedURLException` if the URL is invalid!

### 2. Opening Connections

```java
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(10000);  // 10 seconds
conn.setReadTimeout(10000);
```

### 3. Reading Responses

```java
BufferedReader reader = new BufferedReader(
    new InputStreamReader(conn.getInputStream())
);
String line;
while ((line = reader.readLine()) != null) {
    response.append(line);
}
```

### 4. Handling Exceptions

```java
try {
    URL url = new URL(urlString);
    // ... use the URL
} catch (MalformedURLException e) {
    // Handle invalid URL format
} catch (IOException e) {
    // Handle network errors
}
```

---

## 🎯 Real-World Examples

### Example 1: Motivational Quote

**Request:**

```json
{ "action": "GET_QUOTE" }
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

**Use Case:** Display on task dashboard to motivate users!

### Example 2: User Avatar

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
    "avatarUrl": "https://www.gravatar.com/avatar/abc123?s=200&d=identicon",
    "email": "user@example.com"
  }
}
```

**Use Case:** Show user profile pictures in the task manager!

---

## 🐛 Troubleshooting

### Problem: "Port 8082 already in use"

```powershell
# Find what's using the port
netstat -ano | findstr :8082

# Kill it
taskkill /PID <PID> /F
```

### Problem: "java command not found"

Make sure Java is installed:

```powershell
java -version
```

If not, download from: https://adoptium.net/

### Problem: "ClassNotFoundException: com.google.gson"

The Gson library is missing. It should have been downloaded automatically, but if not:

```powershell
cd backend\lib
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" -OutFile "gson-2.10.1.jar"
```

### Problem: Can't connect to external APIs

- Check your internet connection
- Verify firewall isn't blocking Java
- Try the API URL in a web browser first

---

## 📊 Architecture Overview

```
Client (Test Script)
        │
        │ TCP Connection (JSON)
        ▼
Your URL Service (Port 8082)
        │
        │ HTTPS Requests
        ▼
External APIs (Internet)
    ├── Quotable API (quotes)
    ├── Gravatar (avatars)
    ├── Wttr.in (weather)
    └── Any REST API
```

---

## 📚 Documentation Files

Here's what each file does:

1. **MEMBER3_QUICKSTART.md** (This file!) - Start here
2. **URL_SERVICE_GUIDE.md** - Complete API documentation
3. **URL_EXAMPLES.md** - Code examples and best practices
4. **ARCHITECTURE_DIAGRAM.md** - Visual diagrams
5. **IMPLEMENTATION_SUMMARY.md** - What you built
6. **test-url-service.ps1** - Automated testing script

---

## 🎯 For Your Assignment/Presentation

### What to Show

1. **Start the service**

   ```powershell
   java -cp "bin;lib/*" Main
   ```

   Show it starting on port 8082

2. **Run the tests**

   ```powershell
   powershell -ExecutionPolicy Bypass -File test-url-service.ps1
   ```

   Show all features working

3. **Explain the code**

   - Open `URLIntegrationService.java`
   - Explain the main methods
   - Show exception handling

4. **Discuss concepts**
   - URL vs URI
   - URLConnection
   - HTTP requests
   - Exception handling

### Key Points to Mention

- ✅ "I implemented a TCP server on port 8082"
- ✅ "It uses URL and URLConnection classes from java.net"
- ✅ "I handle MalformedURLException and IOException"
- ✅ "It integrates with 3 external APIs"
- ✅ "Uses JSON for request/response communication"
- ✅ "Implements proper timeout management"

---

## 🚀 Next Steps

1. **Read this file completely** ✓ (You're here!)
2. **Run the service and tests** to see it working
3. **Read URL_SERVICE_GUIDE.md** for detailed info
4. **Review URL_EXAMPLES.md** for code patterns
5. **Understand the code** in URLIntegrationService.java
6. **Prepare your presentation** using the key points above

---

## 💡 Quick Tips

### Before Your Demo

- Test everything once to make sure it works
- Have the service running before showing tests
- Prepare to explain how URL/URLConnection work

### During Your Presentation

- Show the test script running (very visual!)
- Explain one feature in detail (recommend GET_QUOTE)
- Show how you handle exceptions
- Mention real-world use cases

### Common Questions & Answers

**Q: What's the difference between URL and URI?**  
A: URL includes how to access the resource (protocol), URI just identifies it.

**Q: Why use URLConnection?**  
A: It gives us control over timeouts, headers, and request methods.

**Q: What if the external API is down?**  
A: My code handles that with IOException and returns a proper error message.

**Q: Why JSON for communication?**  
A: It's lightweight, human-readable, and widely supported.

---

## ✅ Checklist

Before your presentation, make sure:

- [ ] Service compiles without errors
- [ ] Service starts on port 8082
- [ ] All 8 features work in test script
- [ ] You understand URL/URLConnection
- [ ] You can explain exception handling
- [ ] You know what each feature does
- [ ] You've tested at least once

---

## 🎉 You're Ready!

You've successfully implemented a production-quality URL/URI Integration Service with:

- 8 working features
- 3 external API integrations
- Comprehensive error handling
- Full documentation
- Working test suite

**Great job on Member 3's responsibilities!** 🎊

---

## 📞 Quick Command Reference

```powershell
# Compile
powershell -ExecutionPolicy Bypass -File compile.ps1

# Run
java -cp "bin;lib/*" Main

# Test
powershell -ExecutionPolicy Bypass -File test-url-service.ps1

# Check if running
netstat -ano | findstr :8082
```

---

**Good luck with your assignment! 🚀**
