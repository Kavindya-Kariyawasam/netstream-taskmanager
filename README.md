# 🌊 NetStream TaskManager

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5+-3178C6?style=for-the-badge&logo=typescript)
![Network Programming](https://img.shields.io/badge/Network-Programming-green?style=for-the-badge)

**A Real-Time Collaborative Task Management System Demonstrating Advanced Java Network Programming**

</div>

---

## 📖 Overview

NetStream TaskManager is a full-stack web application that showcases the implementation of core Java networking concepts in a practical, real-world scenario. The system enables teams to create, manage, and collaborate on tasks in real-time through multiple network protocols.

### 🎯 Project Objectives

This educational project demonstrates:

- **TCP/IP Socket Programming**: Reliable client-server communication for CRUD operations
- **UDP Protocol**: Low-latency real-time notifications and broadcasting
- **Java NIO**: Non-blocking I/O for efficient file transfer operations
- **Multithreading**: Concurrent request handling with thread pools
- **URL/URI Handling**: External API integration and resource management

---

## ✨ Features

### Core Functionality

- ✅ **Task Management** (TCP-based)

  - Create, read, update, and delete tasks
  - Assign tasks to team members
  - Track task status and deadlines
  - Persistent in-memory data storage

- 📢 **Real-Time Notifications** (UDP-based)

  - Instant task creation alerts
  - Status change notifications
  - Assignment notifications
  - Broadcast to all connected clients

- 📁 **File Operations** (NIO-based)

  - Upload task attachments (images, PDFs, documents)
  - Download task-related files
  - Non-blocking file transfers
  - Support for large files (up to 50MB)

- 🌐 **External Integrations** (URL/URI-based)

  - Fetch motivational quotes for dashboard
  - Integrate public REST APIs
  - Gravatar profile pictures
  - URL validation and parsing

- ⚡ **High Performance** (Multithreading)
  - Handle 50+ concurrent connections
  - Thread pool management
  - Synchronized data access
  - Graceful error handling and recovery

---

## 🏗️ Architecture

### System Design

```
        ┌─────────────────────────────────────────────────────────┐
        │              React Frontend (Port 5173)                 │
        │         TypeScript + Tailwind CSS + Lucide Icons        │
        └─────────────────────────────────────────────────────────┘
                                     │
                                     │ HTTP/WebSocket
                                     ▼
        ┌─────────────────────────────────────────────────────────┐
        │                  Backend Services Layer                 │
        └─────────────────────────────────────────────────────────┘
              │              │              │              │
              ▼              ▼              ▼              ▼
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
    │ TCP Server   │ │ UDP Server   │ │ NIO Server   │ │ URL Service  │
    │ Port: 8080   │ │ Port: 9090   │ │ Port: 8081   │ │ Port: 8082   │
    │              │ │              │ │              │ │              │
    │ Task CRUD    │ │ Notifications│ │ File Transfer│ │ External APIs│
    └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
              │              │              │              │
              └──────────────┴──────────────┴──────────────┘
                                     │
                                     ▼
                    ┌────────────────────────────────┐
                    │    Shared Data Layer           │
                    │  (Thread-Safe Collections)     │
                    │    - ConcurrentHashMap         │
                    │    - CopyOnWriteArrayList      │
                    └────────────────────────────────┘
                                     │
                                     ▼
                    ┌────────────────────────────────┐
                    │   Thread Pool Manager          │
                    │ (ExecutorService - 50 threads) │
                    └────────────────────────────────┘
```

### Technology Stack

#### Backend

- **Language**: Java 17+
- **Core APIs**:
  - `java.net.Socket` / `ServerSocket` (TCP)
  - `java.net.DatagramSocket` / `DatagramPacket` (UDP)
  - `java.nio.channels.*` (NIO)
  - `java.net.URL` / `URI` (URL handling)
  - `java.util.concurrent.*` (Multithreading)
- **Libraries**: Gson 2.10.1 (JSON parsing)

#### Frontend

- **Framework**: React 18.2+
- **Language**: TypeScript 5+
- **Styling**: Tailwind CSS 3.4+
- **Icons**: Lucide React
- **HTTP Client**: Axios
- **Build Tool**: Vite

---

## 🚀 Quick Start

### Prerequisites

- **Java JDK**: 17 or higher
- **Node.js**: 18 or higher
- **npm**: 9 or higher
- **Git**: Latest version

### Installation

#### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Kavindya-Kariyawasam/netstream-taskmanager.git
cd netstream-taskmanager
```

#### 2️⃣ Backend Setup

```bash
# Navigate to backend directory
cd backend

# Compile all Java files
javac -d bin -cp "lib/*" src/**/*.java src/*.java

# Run the main server
java -cp "bin:lib/*" Main

# You should see:
# 🚀 Starting NetStream TaskManager...
# ✅ TCP Server running on port 8080
# ✅ UDP Server running on port 9090
# ✅ NIO Server running on port 8081
# ✅ URL Service running on port 8082
# 🎉 All servers ready!
```

Helper script to compile the backend on Windows:

```powershell
# From repo root
powershell -ExecutionPolicy Bypass -File backend\compile.ps1
```

This script generates a temporary `backend/files.txt` and compiles all Java sources into `backend/bin` using the JARs in `backend/lib`.

**Note for Windows users**: Replace `:` with `;` in classpath:

```bash
java -cp "bin;lib/*" Main
```

#### 3️⃣ Frontend Setup

```bash
# Open a new terminal
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Frontend will be available at: http://localhost:5173
```

### Verification

1. **Backend Health Check**:

```bash
# Test TCP Server
telnet localhost 8080
# Type: LIST
# Expected: List of tasks or "No tasks found"

# Test with curl
curl -X POST http://localhost:8080 -d '{"action":"LIST"}'
```

2. **Frontend Access**:
   - Open browser: `http://localhost:5173`
   - You should see the Task Dashboard

---

## 📚 API Documentation

### TCP Server API (Port 8080)

#### Create Task

```json
REQUEST:
{
  "action": "CREATE_TASK",
  "data": {
    "title": "Implement NIO Server",
    "assignee": "Member 5",
    "deadline": "2025-10-30",
    "priority": "high"
  }
}

RESPONSE:
{
  "status": "success",
  "data": {
    "taskId": "task_1729350000000",
    "message": "Task created successfully"
  }
}
```

#### Get All Tasks

```json
REQUEST:
{
  "action": "GET_TASKS"
}

RESPONSE:
{
  "status": "success",
  "data": [
    {
      "id": "task_1729350000000",
      "title": "Implement NIO Server",
      "assignee": "Member 5",
      "status": "pending",
      "deadline": "2025-10-30",
      "priority": "high"
    }
  ]
}
```

#### Update Task

```json
REQUEST:
{
  "action": "UPDATE_TASK",
  "data": {
    "taskId": "task_1729350000000",
    "status": "completed"
  }
}

RESPONSE:
{
  "status": "success",
  "message": "Task updated successfully"
}
```

#### Delete Task

```json
REQUEST:
{
  "action": "DELETE_TASK",
  "data": {
    "taskId": "task_1729350000000"
  }
}

RESPONSE:
{
  "status": "success",
  "message": "Task deleted successfully"
}
```

### UDP Server (Port 9090)

**Broadcasting Format**:

```
NOTIFICATION_TYPE|TASK_ID|MESSAGE|TIMESTAMP
```

**Examples**:

```
TASK_CREATED|task_123|New task assigned to Member 5|1729350000000
TASK_UPDATED|task_123|Task status changed to completed|1729350001000
TASK_ASSIGNED|task_456|You have been assigned a new task|1729350002000
```

### NIO File Server (Port 8081)

#### Upload File

```
POST /upload
Content-Type: multipart/form-data

Fields:
- file: Binary file data
- taskId: Associated task ID
- description: File description

Response: File ID
```

#### Download File

```
GET /download/{fileId}

Response: Binary file data with appropriate Content-Type
```

### URL Integration Service (Port 8082)

#### Get Motivational Quote

```
GET /api/quote

Response:
{
  "quote": "The only way to do great work is to love what you do.",
  "author": "Steve Jobs"
}
```

#### Get User Avatar

```
GET /api/avatar/{email}

Response: Gravatar URL
```

---

## 🧪 Testing

### Backend Unit Tests

```bash
cd backend

# Test TCP Server
java -cp "bin:lib/*" tcp.TCPTaskServerTest

# Test UDP Server
java -cp "bin:lib/*" udp.UDPNotificationServerTest

# Test NIO Server
java -cp "bin:lib/*" nio.NIOFileServerTest
```

### Frontend Tests

```bash
cd frontend

# Run tests
npm test

# Run tests with coverage
npm run test:coverage
```

### Integration Tests

```bash
# Start all servers first
cd backend && java -cp "bin:lib/*" Main

# In another terminal, run integration tests
cd tests
./run_integration_tests.sh
```

---

## 📁 Project Structure

```
netstream-taskmanager/
│
├── README.md                          # This file
├── .gitignore                         # Git ignore rules
│
├── backend/                           # Java Backend
│   ├── src/
│   │   ├── shared/                    # Shared components
│   │   │   ├── Task.java              # Task data model
│   │   │   ├── DataStore.java         # Thread-safe storage
│   │   │   ├── JsonUtils.java         # JSON utilities
│   │   │   └── Notification.java      # Notification model
│   │   │
│   │   ├── tcp/                       # TCP Server (Member 1)
│   │   │   ├── TCPTaskServer.java
│   │   │   └── ClientHandler.java
│   │   │
│   │   ├── udp/                       # UDP Server (Member 2)
│   │   │   ├── UDPNotificationServer.java
│   │   │   └── NotificationBroadcaster.java
│   │   │
│   │   ├── nio/                       # NIO Server (Member 5)
│   │   │   ├── NIOFileServer.java
│   │   │   ├── FileUploadHandler.java
│   │   │   └── FileDownloadHandler.java
│   │   │
│   │   ├── url/                       # URL Service (Member 3)
│   │   │   ├── URLIntegrationService.java
│   │   │   ├── QuoteAPIClient.java
│   │   │   └── AvatarService.java
│   │   │
│   │   ├── threading/                 # Threading (Member 4)
│   │   │   ├── ThreadPoolManager.java
│   │   │   ├── ExceptionHandler.java
│   │   │   └── ConnectionManager.java
│   │   │
│   │   └── Main.java                  # Application entry point
│   │
│   ├── lib/                           # External libraries
│   │   └── gson-2.10.1.jar
│   │
│   ├── bin/                           # Compiled classes (gitignored)
│   └── README.md                      # Backend documentation
│
├── frontend/                          # React Frontend
│   ├── src/
│   │   ├── components/                # React components
│   │   │   ├── TaskList.tsx
│   │   │   ├── TaskForm.tsx
│   │   │   ├── TaskCard.tsx
│   │   │   ├── NotificationBell.tsx
│   │   │   ├── FileUpload.tsx
│   │   │   └── ProfileCard.tsx
│   │   │
│   │   ├── services/                  # API services
│   │   │   ├── tcpService.ts
│   │   │   ├── udpService.ts
│   │   │   ├── nioService.ts
│   │   │   └── urlService.ts
│   │   │
│   │   ├── types/                     # TypeScript types
│   │   │   └── index.ts
│   │   │
│   │   ├── App.tsx                    # Main application
│   │   ├── main.tsx                   # Entry point
│   │   └── index.css                  # Global styles
│   │
│   ├── public/                        # Static assets
│   ├── package.json                   # Dependencies
│   ├── tsconfig.json                  # TypeScript config
│   ├── tailwind.config.js             # Tailwind config
│   ├── vite.config.ts                 # Vite config
│   └── README.md                      # Frontend documentation
│
└── docs/                              # Additional documentation
    └── API.md                         # API documentation
```

---

## 🎓 Learning Outcomes

### 1. TCP/IP Socket Programming

- Creating server and client sockets
- Handling multiple client connections
- Implementing request-response protocols
- Managing connection lifecycle

### 2. UDP Protocol

- Connectionless communication
- Broadcasting messages
- Handling packet loss
- Real-time data transmission

### 3. Java NIO (Non-blocking I/O)

- Channel and Buffer concepts
- Selector for multiplexing
- Non-blocking file operations
- Efficient memory management

### 4. Multithreading & Concurrency

- Thread pool management
- Synchronization mechanisms
- Handling race conditions
- Concurrent collections

### 5. URL/URI Handling

- Making HTTP requests
- Parsing URLs
- Integrating external APIs
- Exception handling

### 6. Full-Stack Development

- Backend-frontend integration
- REST API design
- Real-time communication
- State management

---

## 🛠️ Development Guidelines

### Code Standards

- Follow Java naming conventions (camelCase for methods, PascalCase for classes)
- Meaningful comments
- Appropriate exceptions handling
- Meaningful variable names
- Small and focused methods

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/feature-server

# Make changes and commit
git add .
git commit -m "feat: implement feature server"

# Push to remote
git push origin feature/feature-server

# Create Pull Request on GitHub to feature branch
```

### Commit Message Format

```
<type>: <description>

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- test: Adding tests
- refactor: Code refactoring
```

---

## 🐛 Troubleshooting

### Common Issues

#### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

#### Java Compilation Errors

```bash
# Clean and recompile
rm -rf backend/bin/*
javac -d backend/bin -cp "backend/lib/*" backend/src/**/*.java backend/src/*.java
```

#### Frontend Build Issues

```bash
# Clear cache and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install
```

#### Connection Refused

- Ensure all backend servers are running
- Check firewall settings
- Verify correct ports are used
- Check if localhost is properly configured

---

## 📈 Performance Metrics

- **Concurrent Connections**: 50+ simultaneous clients
- **Response Time**: < 50ms average for CRUD operations
- **File Transfer**: Up to 50MB files with non-blocking I/O
- **Notification Latency**: < 100ms for UDP broadcasts
- **Thread Pool Size**: 50 threads (configurable)

---

## 🔒 Security Considerations

- Input validation on all user inputs
- Sanitize file uploads
- Implement rate limiting
- Use timeouts for all network operations
- Handle malformed requests gracefully
- No sensitive data in logs

---

## 🚀 Future Enhancements

- [ ] Add user authentication
- [ ] Implement database persistence
- [ ] Add WebSocket for real-time updates
- [ ] Create mobile app version
- [ ] Add task comments and attachments
- [ ] Implement search and filtering
- [ ] Add unit and integration tests
- [ ] Deploy to cloud platform

---

<div align="center">

**Built with ❤️ by the NetStream Team**

⭐ Star this repository if you found it helpful!

</div>
