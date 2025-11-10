# URL Integration Service - Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    NetStream TaskManager System                          │
└─────────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                  │
├───────────────────────────────────────────────────────────────────────────┤
│  Frontend (React)      PowerShell Script      Java Test Client            │
│  localhost:3000        test-url-service.ps1   URLServiceTester.java       │
└───────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ JSON over TCP
                                    ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                       YOUR URL SERVICE (Member 3)                          │
│                         Port: 8082 (TCP)                                   │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │              URLIntegrationService.java                            │  │
│  ├────────────────────────────────────────────────────────────────────┤  │
│  │  • ServerSocket (port 8082)                                        │  │
│  │  • Socket connection handling                                      │  │
│  │  • JSON request parsing                                            │  │
│  │  • Action routing (8 endpoints)                                    │  │
│  │  • Exception handling                                              │  │
│  │  • Response generation                                             │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                            │
│  ┌─────────────────────────┐  ┌─────────────────────────┐               │
│  │  URL/URI Operations     │  │  File Operations        │               │
│  ├─────────────────────────┤  ├─────────────────────────┤               │
│  │ • VALIDATE_URL          │  │ • DOWNLOAD_FILE         │               │
│  │ • PARSE_URL             │  │ • UPLOAD_FILE           │               │
│  │   - Protocol            │  │   - Save to uploads/    │               │
│  │   - Host, Port          │  │   - Size validation     │               │
│  │   - Path, Query         │  │                         │               │
│  └─────────────────────────┘  └─────────────────────────┘               │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────┐    │
│  │              External API Integration Layer                       │    │
│  ├──────────────────────────────────────────────────────────────────┤    │
│  │  • GET_QUOTE      - Quotable API                                 │    │
│  │  • GET_AVATAR     - Gravatar API                                 │    │
│  │  • GET_WEATHER    - Wttr.in API                                  │    │
│  │  • FETCH_API      - Generic REST API caller                      │    │
│  └──────────────────────────────────────────────────────────────────┘    │
│                                                                            │
└───────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS
                                    ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                        EXTERNAL APIs (Internet)                            │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │  Quotable API    │  │  Gravatar API    │  │  Wttr.in API     │       │
│  │  ──────────────  │  │  ──────────────  │  │  ──────────────  │       │
│  │  Random quotes   │  │  Avatar images   │  │  Weather data    │       │
│  │  api.quotable.io │  │  gravatar.com    │  │  wttr.in         │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
│                                                                            │
│  ┌────────────────────────────────────────────────────────────────┐      │
│  │           Any REST API (Generic Fetcher)                        │      │
│  │  • jsonplaceholder.typicode.com                                 │      │
│  │  • api.github.com                                               │      │
│  │  • Your custom APIs                                             │      │
│  └────────────────────────────────────────────────────────────────┘      │
│                                                                            │
└───────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════
                            REQUEST FLOW DIAGRAM
═══════════════════════════════════════════════════════════════════════════

  Client                     URL Service                    External API
  ──────                     ───────────                    ────────────

    │                              │                              │
    │  1. TCP Connection           │                              │
    ├──────────────────────────────►                              │
    │                              │                              │
    │  2. JSON Request             │                              │
    │  {"action":"GET_QUOTE"}      │                              │
    ├──────────────────────────────►                              │
    │                              │                              │
    │                              │  3. HTTPS GET Request        │
    │                              ├──────────────────────────────►
    │                              │                              │
    │                              │  4. JSON Response            │
    │                              │◄──────────────────────────────┤
    │                              │                              │
    │                              │  5. Process & Format         │
    │                              │     (Error handling)         │
    │                              │                              │
    │  6. JSON Response            │                              │
    │  {"status":"success",...}    │                              │
    │◄──────────────────────────────┤                              │
    │                              │                              │
    │  7. Close Connection         │                              │
    │◄──────────────────────────────┤                              │
    │                              │                              │


═══════════════════════════════════════════════════════════════════════════
                      JAVA CLASSES USED (Member 3)
═══════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────┐
│                         Network Classes                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  java.net.URL                                                            │
│  ├── Purpose: Parse and represent URLs                                  │
│  ├── Methods: getProtocol(), getHost(), getPort(), getPath()            │
│  └── Throws: MalformedURLException                                      │
│                                                                          │
│  java.net.URI                                                            │
│  ├── Purpose: More flexible URL/URI parsing                             │
│  └── Methods: Similar to URL but more robust                            │
│                                                                          │
│  java.net.URLConnection                                                  │
│  ├── Purpose: Open connection to URL                                    │
│  ├── Methods: setConnectTimeout(), setReadTimeout()                     │
│  ├──          getInputStream(), getOutputStream()                       │
│  └── Throws: IOException                                                │
│                                                                          │
│  java.net.HttpURLConnection                                              │
│  ├── Purpose: HTTP-specific features                                    │
│  ├── Methods: setRequestMethod("GET"/"POST")                            │
│  ├──          getResponseCode()                                         │
│  └── Inherits: URLConnection                                            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         Exception Hierarchy                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Exception                                                               │
│    └── IOException                                                       │
│          ├── SocketException                                            │
│          │     └── SocketTimeoutException ← Connection timeout          │
│          ├── UnknownHostException ← DNS failure                         │
│          └── MalformedURLException ← Invalid URL format                 │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════
                         DATA FLOW (JSON Protocol)
═══════════════════════════════════════════════════════════════════════════

REQUEST FORMAT:
┌────────────────────────────────────────────────────────────────────────┐
│ {                                                                       │
│   "action": "ACTION_NAME",                                             │
│   "data": {                                                            │
│     "key1": "value1",                                                  │
│     "key2": "value2"                                                   │
│   }                                                                    │
│ }                                                                       │
└────────────────────────────────────────────────────────────────────────┘

RESPONSE FORMAT (Success):
┌────────────────────────────────────────────────────────────────────────┐
│ {                                                                       │
│   "status": "success",                                                 │
│   "data": {                                                            │
│     ... response data ...                                              │
│   }                                                                    │
│ }                                                                       │
└────────────────────────────────────────────────────────────────────────┘

RESPONSE FORMAT (Error):
┌────────────────────────────────────────────────────────────────────────┐
│ {                                                                       │
│   "status": "error",                                                   │
│   "message": "Error description"                                       │
│ }                                                                       │
└────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════
                      SYSTEM INTEGRATION (All Members)
═══════════════════════════════════════════════════════════════════════════

                          ┌──────────────────┐
                          │   Main.java      │
                          │   (Startup)      │
                          └────────┬─────────┘
                                   │
           ┌───────────────────────┼───────────────────────┐
           │                       │                       │
           ▼                       ▼                       ▼
  ┌────────────────┐    ┌─────────────────┐    ┌──────────────────┐
  │  TCP Server    │    │  HTTP Gateway   │    │  URL Service     │
  │  (Member 1)    │    │                 │    │  (Member 3 - YOU)│
  │  Port: 8080    │    │  Port: 3000     │    │  Port: 8082      │
  └────────────────┘    └─────────────────┘    └──────────────────┘
           │                                              │
           │                                              │
           ▼                                              ▼
  ┌────────────────────────────────────────────────────────────────┐
  │                    Shared Components                            │
  ├────────────────────────────────────────────────────────────────┤
  │  • DataStore.java (Thread-safe storage)                        │
  │  • JsonUtils.java (JSON serialization)                         │
  │  • Task.java (Data model)                                      │
  │  • ThreadPoolManager.java (Member 4)                           │
  └────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════
                           FILE STRUCTURE
═══════════════════════════════════════════════════════════════════════════

backend/
├── src/
│   ├── url/  ← YOUR IMPLEMENTATION
│   │   ├── URLIntegrationService.java  (Main service - 548 lines)
│   │   └── URLServiceTester.java       (Test client - 160 lines)
│   │
│   ├── shared/
│   │   ├── JsonUtils.java              (Used for JSON)
│   │   ├── Task.java                   (Task model)
│   │   └── DataStore.java              (Shared storage)
│   │
│   └── Main.java                       (Updated to start URL service)
│
├── lib/
│   └── gson-2.10.1.jar                 (JSON library)
│
├── uploads/                             (File storage)
│
├── test-url-service.ps1                 (PowerShell test script)
├── MEMBER3_QUICKSTART.md                (Getting started guide)
├── URL_SERVICE_GUIDE.md                 (Detailed documentation)
├── URL_EXAMPLES.md                      (Code examples)
└── IMPLEMENTATION_SUMMARY.md            (This summary)


═══════════════════════════════════════════════════════════════════════════
                         KEY IMPLEMENTATION POINTS
═══════════════════════════════════════════════════════════════════════════

1. SERVER SOCKET (TCP)
   ┌──────────────────────────────────────────────────────────────┐
   │ ServerSocket serverSocket = new ServerSocket(8082);          │
   │ Socket clientSocket = serverSocket.accept();                 │
   └──────────────────────────────────────────────────────────────┘

2. URL CONNECTION (HTTP)
   ┌──────────────────────────────────────────────────────────────┐
   │ URL url = new URL("https://api.example.com");               │
   │ HttpURLConnection conn = (HttpURLConnection)                 │
   │                          url.openConnection();               │
   │ conn.setRequestMethod("GET");                                │
   │ conn.setConnectTimeout(10000);                               │
   └──────────────────────────────────────────────────────────────┘

3. EXCEPTION HANDLING
   ┌──────────────────────────────────────────────────────────────┐
   │ try {                                                         │
   │     URL url = new URL(urlString);                            │
   │ } catch (MalformedURLException e) {                          │
   │     return JsonUtils.createErrorResponse(e.getMessage());    │
   │ } catch (IOException e) {                                    │
   │     return JsonUtils.createErrorResponse(e.getMessage());    │
   │ }                                                            │
   └──────────────────────────────────────────────────────────────┘

4. JSON PROCESSING
   ┌──────────────────────────────────────────────────────────────┐
   │ JsonObject request = JsonParser.parseString(json)            │
   │                               .getAsJsonObject();            │
   │ String action = request.get("action").getAsString();         │
   │ return JsonUtils.createSuccessResponse(data);                │
   └──────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════
                          TESTING OVERVIEW
═══════════════════════════════════════════════════════════════════════════

┌───────────────────────────────────────────────────────────────────────┐
│                        Testing Methods                                 │
├───────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  Method 1: PowerShell Test Script (Recommended)                       │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ powershell -ExecutionPolicy Bypass -File test-url-service.ps1  │ │
│  │                                                                 │ │
│  │ ✓ Tests all 8 features automatically                           │ │
│  │ ✓ Color-coded output                                           │ │
│  │ ✓ Easy to run                                                  │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│  Method 2: Java Test Client                                           │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ java -cp "bin;lib/*" url.URLServiceTester                      │ │
│  │                                                                 │ │
│  │ ✓ Java-based testing                                           │ │
│  │ ✓ Shows request/response flow                                  │ │
│  │ ✓ Easy to modify for custom tests                              │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│  Method 3: Manual PowerShell Commands                                 │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ $client = New-Object System.Net.Sockets.TcpClient(...)         │ │
│  │ # ... send request ...                                          │ │
│  │                                                                 │ │
│  │ ✓ Full control                                                 │ │
│  │ ✓ Good for debugging                                           │ │
│  │ ✓ See exact network communication                              │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                        │
└───────────────────────────────────────────────────────────────────────┘
```
