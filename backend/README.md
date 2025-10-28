# NetStream TaskManager - Backend

Java-based backend services demonstrating network programming concepts through TCP, UDP, NIO, and URL handling.

## 🎯 Overview

This backend system consists of four independent Java servers running simultaneously, each demonstrating different network programming concepts:

1. **TCP Server** (Port 8080) - Task CRUD operations using Socket/ServerSocket
2. **UDP Server** (Port 9090) - Real-time notifications using DatagramSocket
3. **NIO Server** (Port 8081) - File operations using non-blocking Channels and Selectors
4. **URL Service** (Port 8082) - External API integration using URL/URLConnection

All servers share a common thread-safe data layer and use a centralized thread pool for concurrent request handling.

---

## 🚀 Quick Start

### Prerequisites

- Java JDK 17 or higher
- Gson library (for JSON processing)

### Setup

```bash
# Navigate to backend directory
cd backend

# Download Gson library
cd lib
curl -O https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
cd ..

# Compile all Java files
javac -d bin -cp "lib/*" src/shared/*.java src/tcp/*.java src/udp/*.java src/nio/*.java src/url/*.java src/threading/*.java src/Main.java

# Run the application
java -cp "bin:lib/*" Main
```

**Note for Windows users**: Use semicolon (`;`) instead of colon (`:`) in classpath:

```bash
javac -d bin -cp "lib/*" src/shared/*.java src/tcp/*.java src/Main.java
java -cp "bin;lib/*" Main
```

### Expected Output

```
Starting NetStream TaskManager...
==================================================
Thread pool initialized with 50 threads
TCP Server started on port 8080
Listening for client connections...
UDP Server started on port 9090
NIO Server started on port 8081
URL Service started on port 8082
==================================================
All servers ready!
Press Ctrl+C to stop
```

---

## �️ Quick build helper

For easier compiling of the backend locally, a small PowerShell helper script is included:

```powershell
# From the repository root or anywhere, run:
# (runs the script located at backend/compile.ps1)
powershell -ExecutionPolicy Bypass -File backend\compile.ps1
```

Notes:

- The script generates a temporary `backend/files.txt` argfile and compiles sources into `backend/bin` using `backend/lib/gson-2.10.1.jar`.
- Windows users can run the script directly in PowerShell. Unix users can still use the `javac` commands shown above.

### VS Code (Java) troubleshooting

If the editor reports unresolved imports for Gson even though `backend/lib/gson-2.10.1.jar` exists, reload the Java language server:

1. Ctrl+Shift+P → "Java: Clean the Java language server workspace"
2. Ctrl+Shift+P → "Developer: Reload Window"

Alternatively, open the `backend` folder directly in VS Code so `backend/.vscode/settings.json` is applied.

---

## �📁 Project Structure

```
backend/
├── src/
│   ├── shared/                    # Shared components (used by all servers)
│   │   ├── Task.java              # Task data model
│   │   ├── DataStore.java         # Thread-safe in-memory storage
│   │   └── JsonUtils.java         # JSON serialization utilities
│   │
│   ├── tcp/                       # TCP Server
│   │   └── TCPTaskServer.java     # Socket-based CRUD operations
│   │
│   ├── udp/                       # UDP Server
│   │   └── UDPNotificationServer.java  # DatagramSocket broadcasting
│   │
│   ├── nio/                       # NIO Server
│   │   └── NIOFileServer.java     # Non-blocking file I/O
│   │
│   ├── url/                       # URL Service
│   │   └── URLIntegrationService.java  # External API integration
│   │
│   ├── threading/                 # Threading utilities
│   │   ├── ThreadPoolManager.java      # ExecutorService management
│   │   └── ExceptionHandler.java       # Centralized exception handling
│   │
│   └── Main.java                  # Application entry point
│
├── lib/                           # External libraries
│   └── gson-2.10.1.jar           # JSON processing library
│
├── bin/                           # Compiled .class files (gitignored)
├── uploads/                       # File upload storage (gitignored)
└── README.md                      # This file
```

---

## 🔧 Component Details

### Shared Components

#### `Task.java`

Task data model with the following fields:

- `id` (String) - Unique identifier (auto-generated)
- `title` (String) - Task title
- `assignee` (String) - Person assigned to the task
- `status` (String) - "pending", "in-progress", or "completed"
- `deadline` (String) - ISO date format
- `priority` (String) - "low", "medium", or "high"
- `createdAt` (String) - Creation timestamp
- `updatedAt` (String) - Last update timestamp

#### `DataStore.java`

Thread-safe in-memory storage using `ConcurrentHashMap`:

- `addTask(Task)` - Add new task
- `getTask(String id)` - Retrieve task by ID
- `getAllTasks()` - Get all tasks as list
- `updateTask(String id, Task)` - Update existing task
- `deleteTask(String id)` - Remove task
- `addNotification(String)` - Store notification for UDP broadcasting

#### `JsonUtils.java`

JSON serialization utilities using Gson:

- `toJson(Object)` - Convert object to JSON string
- `fromJson(String, Class)` - Parse JSON to object
- `parseJson(String)` - Parse to JsonObject
- `createSuccessResponse(Object)` - Create success response
- `createErrorResponse(String)` - Create error response

---

### TCP Server (Port 8080)

#### `TCPTaskServer.java`

**Purpose**: Handle CRUD operations for tasks using TCP sockets.

**Key Features**:

- ServerSocket listening on port 8080
- JSON-based request/response protocol
- Connection timeout management (5 seconds)
- Thread pool integration for concurrent clients
- Exception handling with ExceptionHandler

**Supported Actions**:

1. **CREATE_TASK**

   ```json
   Request:
   {
     "action": "CREATE_TASK",
     "data": {
       "title": "Implement NIO Server",
       "assignee": "Member 5",
       "deadline": "2025-10-30",
       "priority": "high"
     }
   }

   Response:
   {
     "status": "success",
     "data": {
       "taskId": "task_1729350000000",
       "message": "Task created successfully"
     }
   }
   ```

2. **GET_TASKS**

   ```json
   Request:
   {
     "action": "GET_TASKS"
   }

   Response:
   {
     "status": "success",
     "data": [
       {
         "id": "task_123",
         "title": "Build TCP Server",
         "assignee": "Member 1",
         "status": "completed",
         ...
       }
     ]
   }
   ```

3. **UPDATE_TASK**

   ```json
   Request:
   {
     "action": "UPDATE_TASK",
     "data": {
       "taskId": "task_123",
       "status": "completed"
     }
   }

   Response:
   {
     "status": "success",
     "message": "Task updated successfully"
   }
   ```

4. **DELETE_TASK**

   ```json
   Request:
   {
     "action": "DELETE_TASK",
     "data": {
       "taskId": "task_123"
     }
   }

   Response:
   {
     "status": "success",
     "message": "Task deleted successfully"
   }
   ```

---

### UDP Server (Port 9090)

#### `UDPNotificationServer.java`

**Purpose**: Broadcast real-time notifications to all connected clients.

**Key Features**:

- DatagramSocket for connectionless communication
- Broadcasts task creation, update, and deletion events
- Maintains list of subscribed client addresses
- Background thread monitoring DataStore for changes

**Notification Format**:

```
NOTIFICATION_TYPE|TASK_ID|MESSAGE|TIMESTAMP
```

**Examples**:

```
TASK_CREATED|task_123|New task assigned to Member 5|1729350000000
TASK_UPDATED|task_123|Task status changed to completed|1729350001000
TASK_DELETED|task_456|Task deleted|1729350002000
```

---

### NIO Server (Port 8081)

#### `NIOFileServer.java`

**Purpose**: Handle file uploads and downloads using non-blocking I/O.

**Key Features**:

- ServerSocketChannel for accepting connections
- Selector for multiplexing multiple channels
- ByteBuffer operations (flip, clear, compact, rewind)
- Non-blocking file reads and writes
- Support for concurrent file transfers

**Operations**:

- File upload with progress tracking
- File download by file ID
- Maximum file size: 50MB
- Stored in `backend/uploads/` directory

---

### URL Service (Port 8082)

#### `URLIntegrationService.java`

**Purpose**: Integrate with external REST APIs using URL/URLConnection.

**Key Features**:

- URL class for parsing and validation
- URLConnection for HTTP requests
- Integration with public APIs
- Gravatar avatar URL generation

**Endpoints**:

1. **Get Motivational Quote**

   - Fetches from: `https://api.quotable.io/random`
   - Returns: `{ "quote": "...", "author": "..." }`

2. **Get Gravatar Avatar**
   - Generates URL from email hash
   - Returns: Gravatar image URL

---

### Threading Components

#### `ThreadPoolManager.java`

**Purpose**: Manage thread pool for concurrent request handling.

**Key Features**:

- FixedThreadPool with 50 threads
- Singleton pattern for global access
- Graceful shutdown handling

**Usage**:

```java
ExecutorService pool = ThreadPoolManager.getThreadPool();
pool.submit(() -> handleClient(socket));
```

#### `ExceptionHandler.java`

**Purpose**: Centralized exception handling and logging.

**Key Features**:

- Formatted error messages with timestamps
- Specific handling for network exceptions
- Stack trace logging
- Context-aware error reporting

**Handled Exceptions**:

- `IOException` - General I/O errors
- `SocketException` - Socket connection errors
- `SocketTimeoutException` - Connection timeouts
- `UnknownHostException` - Host resolution failures

**Usage**:

```java
try {
    // Network operation
} catch (IOException e) {
    ExceptionHandler.handle(e, "TCP Server");
}
```

---

## 🧪 Testing

### Testing TCP Server

#### Using telnet:

```bash
telnet localhost 8080
```

Then type JSON request:

```json
{
  "action": "CREATE_TASK",
  "data": { "title": "Test Task", "assignee": "John Doe", "priority": "high" }
}
```

#### Using curl:

```bash
# Create task
curl -X POST http://localhost:8080 -d '{"action":"CREATE_TASK","data":{"title":"Test Task","assignee":"John","priority":"high"}}'

# Get all tasks
curl -X POST http://localhost:8080 -d '{"action":"GET_TASKS"}'

# Update task
curl -X POST http://localhost:8080 -d '{"action":"UPDATE_TASK","data":{"taskId":"task_123","status":"completed"}}'

# Delete task
curl -X POST http://localhost:8080 -d '{"action":"DELETE_TASK","data":{"taskId":"task_123"}}'
```

### Testing UDP Server

Using netcat to listen for broadcasts:

```bash
nc -u -l 9091
```

(UDP server will broadcast to this port when tasks are created/updated)

### Testing NIO Server

Upload a file:

```bash
curl -X POST -F "file=@test.pdf" -F "taskId=task_123" http://localhost:8081/upload
```

### Testing URL Service

```bash
# Get motivational quote
curl http://localhost:8082/api/quote

# Get avatar URL
curl http://localhost:8082/api/avatar/test@example.com
```

---

## 🔍 Key Java Concepts Demonstrated

### 1. TCP/IP Socket Programming

- **Classes**: `Socket`, `ServerSocket`, `BufferedReader`, `PrintWriter`
- **Concepts**: Connection establishment, three-way handshake, reliable data transfer
- **Location**: `tcp/TCPTaskServer.java`

### 2. UDP Protocol

- **Classes**: `DatagramSocket`, `DatagramPacket`, `InetAddress`
- **Concepts**: Connectionless communication, broadcasting, packet handling
- **Location**: `udp/UDPNotificationServer.java`

### 3. Java NIO

- **Classes**: `ServerSocketChannel`, `SocketChannel`, `Selector`, `ByteBuffer`, `FileChannel`
- **Concepts**: Non-blocking I/O, channel multiplexing, buffer operations
- **Location**: `nio/NIOFileServer.java`

### 4. URL/URI Handling

- **Classes**: `URL`, `URI`, `URLConnection`, `HttpURLConnection`
- **Concepts**: HTTP requests, URL parsing, external API integration
- **Location**: `url/URLIntegrationService.java`

### 5. Multithreading

- **Classes**: `ExecutorService`, `Executors`, `Thread`, `Runnable`
- **Concepts**: Thread pools, concurrent execution, synchronization
- **Location**: `threading/ThreadPoolManager.java`

### 6. Thread Safety

- **Classes**: `ConcurrentHashMap`, `Collections.synchronizedList()`, `synchronized` keyword
- **Concepts**: Race conditions, deadlock prevention, atomic operations
- **Location**: `shared/DataStore.java`

---

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Check what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>

# On Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Compilation Errors

```bash
# Clean and recompile
rm -rf bin/*
javac -d bin -cp "lib/*" src/**/*.java src/*.java
```

### ClassNotFoundException

Make sure Gson library is in the `lib/` directory and included in classpath:

```bash
java -cp "bin:lib/*" Main
```

### Connection Refused from Frontend

1. Verify all servers are running
2. Check firewall settings
3. Ensure ports 8080, 9090, 8081, 8082 are not blocked
4. Check if `localhost` resolves correctly

### Thread Pool Not Working

Ensure Member 4's `ThreadPoolManager` is initialized before use:

```java
ExecutorService pool = ThreadPoolManager.getThreadPool();
```

---

## 📊 Performance Metrics

- **Concurrent Connections**: 50+ simultaneous clients
- **Response Time**: < 50ms average for CRUD operations
- **File Transfer**: Up to 50MB files with non-blocking I/O
- **Notification Latency**: < 100ms for UDP broadcasts
- **Thread Pool Size**: 50 threads (configurable)

---

## 🔐 Security Considerations

- Input validation on all requests
- Sanitize file uploads (check file types, sizes)
- Set connection timeouts (5 seconds default)
- Handle malformed JSON gracefully
- No sensitive data in logs
- Exception messages don't expose internal details

---

## 📚 Dependencies

### External Libraries

- **Gson 2.10.1**: JSON serialization/deserialization
  - Maven: `com.google.code.gson:gson:2.10.1`
  - Download: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

### Standard Libraries

- `java.net.*` - Socket, ServerSocket, DatagramSocket, URL
- `java.nio.*` - Channels, Buffers, Selectors
- `java.io.*` - BufferedReader, PrintWriter, File I/O
- `java.util.concurrent.*` - ExecutorService, Thread pools
- `java.time.*` - Date/time handling
